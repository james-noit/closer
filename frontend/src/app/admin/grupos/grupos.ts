import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { GrupoPersonasService } from '../../grupo-personas/grupo-personas.service';
import { GrupoPersonas } from '../../models/grupo-personas.model';

@Component({
  selector: 'app-admin-grupos',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './grupos.html',
  styleUrl: './grupos.scss'
})
export class AdminGrupos implements OnInit {
  readonly grupos = signal<GrupoPersonas[]>([]);
  readonly loading = signal(false);
  readonly errorMsg = signal<string | null>(null);
  readonly successMsg = signal<string | null>(null);

  nuevoNombre = '';
  editandoId: number | null = null;
  editandoNombre = '';

  constructor(private grupoPersonasService: GrupoPersonasService) {}

  ngOnInit(): void {
    this.cargarGrupos();
  }

  cargarGrupos(): void {
    this.loading.set(true);
    this.errorMsg.set(null);
    this.grupoPersonasService.getGrupos().subscribe({
      next: data => {
        this.grupos.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.errorMsg.set('No se pudieron cargar los grupos.');
        this.loading.set(false);
      }
    });
  }

  crearGrupo(): void {
    if (!this.nuevoNombre.trim()) return;
    this.grupoPersonasService.createGrupo({ nombre: this.nuevoNombre.trim() }).subscribe({
      next: () => {
        this.nuevoNombre = '';
        this.successMsg.set('Grupo creado correctamente.');
        setTimeout(() => this.successMsg.set(null), 3000);
        this.cargarGrupos();
      },
      error: () => this.errorMsg.set('No se pudo crear el grupo.')
    });
  }

  iniciarEdicion(grupo: GrupoPersonas): void {
    this.editandoId = grupo.id ?? null;
    this.editandoNombre = grupo.nombre;
  }

  guardarEdicion(grupo: GrupoPersonas): void {
    if (!grupo.id) return;
    this.grupoPersonasService.updateGrupo(grupo.id, { ...grupo, nombre: this.editandoNombre }).subscribe({
      next: () => {
        this.editandoId = null;
        this.successMsg.set('Grupo actualizado correctamente.');
        setTimeout(() => this.successMsg.set(null), 3000);
        this.cargarGrupos();
      },
      error: () => this.errorMsg.set('No se pudo actualizar el grupo.')
    });
  }

  cancelarEdicion(): void {
    this.editandoId = null;
    this.editandoNombre = '';
  }

  eliminarGrupo(id: number): void {
    if (!confirm('¿Estás seguro de que deseas eliminar este grupo?')) return;
    this.grupoPersonasService.deleteGrupo(id).subscribe({
      next: () => {
        this.successMsg.set('Grupo eliminado correctamente.');
        setTimeout(() => this.successMsg.set(null), 3000);
        this.cargarGrupos();
      },
      error: () => this.errorMsg.set('No se pudo eliminar el grupo.')
    });
  }
}
