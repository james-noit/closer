import { Component, OnInit, signal, computed } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { PersonaService } from '../persona/persona.service';
import { Contacto } from '../models/contacto.model';

@Component({
  selector: 'app-home',
  imports: [FormsModule],
  templateUrl: './home.html',
  styleUrl: './home.scss'
})
export class Home implements OnInit {
  readonly personas = signal<Contacto[]>([]);
  readonly loading = signal(false);
  readonly errorMsg = signal<string | null>(null);

  readonly filtroNombre = signal('');
  readonly filtroApellidos = signal('');
  readonly filtroTelefono = signal('');
  readonly filtroEmail = signal('');
  readonly filtroCumpleanos = signal('');

  readonly personasFiltradas = computed(() => {
    const nombre = this.filtroNombre().toLowerCase();
    const apellidos = this.filtroApellidos().toLowerCase();
    const telefono = this.filtroTelefono().toLowerCase();
    const email = this.filtroEmail().toLowerCase();
    const cumpleanos = this.filtroCumpleanos().toLowerCase();

    return this.personas().filter(p =>
      p.nombre.toLowerCase().includes(nombre) &&
      p.apellidos.toLowerCase().includes(apellidos) &&
      p.numeroTelefono.toLowerCase().includes(telefono) &&
      (p.email ?? '').toLowerCase().includes(email) &&
      p.fechaCumpleanos.toLowerCase().includes(cumpleanos)
    );
  });

  constructor(private personaService: PersonaService) {}

  ngOnInit(): void {
    this.cargarPersonas();
  }

  cargarPersonas(): void {
    this.loading.set(true);
    this.errorMsg.set(null);
    this.personaService.getPersonas().subscribe({
      next: data => {
        this.personas.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.errorMsg.set('No se pudieron cargar las personas. Inténtalo de nuevo.');
        this.loading.set(false);
      }
    });
  }
}

