import { Component, OnInit, signal, computed } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { PersonaService } from '../persona/persona.service';
import { GrupoPersonasService } from '../grupo-personas/grupo-personas.service';
import { AuthService } from '../auth/auth.service';
import { Contacto } from '../models/contacto.model';
import { GrupoPersonas } from '../models/grupo-personas.model';

interface NuevaPersonaForm {
  nombre: string;
  apellidos: string;
  numeroTelefono: string;
  fechaCumpleanos: string;
  email: string;
  grupoPersonasId: number | null;
}

@Component({
  selector: 'app-home',
  imports: [FormsModule],
  templateUrl: './home.html',
  styleUrl: './home.scss'
})
export class Home implements OnInit {
  readonly personas = signal<Contacto[]>([]);
  readonly grupos = signal<GrupoPersonas[]>([]);
  readonly loading = signal(false);
  readonly errorMsg = signal<string | null>(null);
  readonly successMsg = signal<string | null>(null);

  readonly tabActivo = signal<number | null>(null);

  readonly filtroNombre = signal('');
  readonly filtroApellidos = signal('');
  readonly filtroTelefono = signal('');
  readonly filtroEmail = signal('');
  readonly filtroCumpleanos = signal('');

  readonly isAdmin: boolean;

  mostrarFormNuevaPersona = false;
  guardandoPersona = false;
  nuevaPersona: NuevaPersonaForm = this.nuevaPersonaVacia();

  readonly grupoActivo = computed(() => this.grupos().find(g => g.id === this.tabActivo()));

  readonly personasDelTabActivo = computed(() => {
    const tabId = this.tabActivo();
    if (tabId === null) return [];
    const nombre = this.filtroNombre().toLowerCase();
    const apellidos = this.filtroApellidos().toLowerCase();
    const telefono = this.filtroTelefono().toLowerCase();
    const email = this.filtroEmail().toLowerCase();
    const cumpleanos = this.filtroCumpleanos().toLowerCase();

    return this.personas().filter(p => {
      const enGrupo = p.grupoPersonas?.id === tabId;
      return enGrupo &&
        p.nombre.toLowerCase().includes(nombre) &&
        p.apellidos.toLowerCase().includes(apellidos) &&
        p.numeroTelefono.toLowerCase().includes(telefono) &&
        (p.email ?? '').toLowerCase().includes(email) &&
        p.fechaCumpleanos.toLowerCase().includes(cumpleanos);
    });
  });

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

  constructor(
    private personaService: PersonaService,
    private grupoPersonasService: GrupoPersonasService,
    private authService: AuthService
  ) {
    this.isAdmin = authService.isAdmin();
  }

  ngOnInit(): void {
    this.cargarDatos();
  }

  cargarDatos(): void {
    this.loading.set(true);
    this.errorMsg.set(null);
    this.grupoPersonasService.getGrupos().subscribe({
      next: grupos => {
        this.grupos.set(grupos);
        if (grupos.length > 0 && this.tabActivo() === null) {
          this.tabActivo.set(grupos[0].id ?? null);
        }
        this.cargarPersonas();
      },
      error: () => {
        this.errorMsg.set('No se pudieron cargar los grupos.');
        this.loading.set(false);
      }
    });
  }

  cargarPersonas(): void {
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

  seleccionarTab(grupoId: number): void {
    this.tabActivo.set(grupoId);
    this.limpiarFiltros();
    this.mostrarFormNuevaPersona = false;
  }

  limpiarFiltros(): void {
    this.filtroNombre.set('');
    this.filtroApellidos.set('');
    this.filtroTelefono.set('');
    this.filtroEmail.set('');
    this.filtroCumpleanos.set('');
  }

  toggleFormNuevaPersona(): void {
    this.mostrarFormNuevaPersona = !this.mostrarFormNuevaPersona;
    if (this.mostrarFormNuevaPersona) {
      this.nuevaPersona = this.nuevaPersonaVacia();
      this.nuevaPersona.grupoPersonasId = this.tabActivo();
    }
  }

  guardarNuevaPersona(): void {
    if (!this.nuevaPersona.nombre.trim() || !this.nuevaPersona.apellidos.trim() ||
        !this.nuevaPersona.numeroTelefono.trim() || !this.nuevaPersona.fechaCumpleanos) {
      this.errorMsg.set('Por favor, rellena los campos obligatorios.');
      return;
    }
    this.guardandoPersona = true;
    const persona: Contacto = {
      nombre: this.nuevaPersona.nombre,
      apellidos: this.nuevaPersona.apellidos,
      numeroTelefono: this.nuevaPersona.numeroTelefono,
      fechaCumpleanos: this.nuevaPersona.fechaCumpleanos,
      email: this.nuevaPersona.email || undefined,
      grupoPersonas: this.nuevaPersona.grupoPersonasId
        ? { id: this.nuevaPersona.grupoPersonasId, nombre: '' }
        : undefined
    };
    this.personaService.createPersona(persona).subscribe({
      next: () => {
        this.mostrarFormNuevaPersona = false;
        this.guardandoPersona = false;
        this.successMsg.set('Persona creada correctamente.');
        setTimeout(() => this.successMsg.set(null), 3000);
        this.cargarPersonas();
      },
      error: () => {
        this.guardandoPersona = false;
        this.errorMsg.set('No se pudo crear la persona.');
      }
    });
  }

  cambiarGrupo(persona: Contacto, grupoId: string): void {
    if (!persona.id) return;
    const gid = parseInt(grupoId, 10);
    const grupo = this.grupos().find(g => g.id === gid);
    const updated: Contacto = {
      ...persona,
      grupoPersonas: grupo ? { id: gid, nombre: grupo.nombre } : undefined
    };
    this.personaService.updatePersona(persona.id, updated).subscribe({
      next: () => {
        this.successMsg.set('Grupo actualizado.');
        setTimeout(() => this.successMsg.set(null), 3000);
        this.cargarPersonas();
      },
      error: () => this.errorMsg.set('No se pudo cambiar el grupo.')
    });
  }

  eliminarPersona(id: number): void {
    if (!confirm('¿Seguro que deseas eliminar esta persona?')) return;
    this.personaService.deletePersona(id).subscribe({
      next: () => {
        this.successMsg.set('Persona eliminada.');
        setTimeout(() => this.successMsg.set(null), 3000);
        this.cargarPersonas();
      },
      error: () => this.errorMsg.set('No se pudo eliminar la persona.')
    });
  }

  private nuevaPersonaVacia(): NuevaPersonaForm {
    return { nombre: '', apellidos: '', numeroTelefono: '', fechaCumpleanos: '', email: '', grupoPersonasId: null };
  }
}

