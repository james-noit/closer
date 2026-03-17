import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';

import { Home } from './home';
import { PersonaService } from '../persona/persona.service';
import { GrupoPersonasService } from '../grupo-personas/grupo-personas.service';
import { AuthService } from '../auth/auth.service';
import { Contacto } from '../models/contacto.model';
import { GrupoPersonas } from '../models/grupo-personas.model';

const dummyGrupos: GrupoPersonas[] = [
  { id: 1, nombre: 'Amigos' },
  { id: 2, nombre: 'Familia' }
];

const dummyPersonas: Contacto[] = [
  { id: 1, nombre: 'Ana', apellidos: 'García', numeroTelefono: '+34600000001', fechaCumpleanos: '1990-03-15', email: 'ana@example.com', grupoPersonas: { id: 1, nombre: 'Amigos' } },
  { id: 2, nombre: 'Luis', apellidos: 'Martínez', numeroTelefono: '+34600000002', fechaCumpleanos: '1985-07-22', grupoPersonas: { id: 1, nombre: 'Amigos' } }
];

describe('Home', () => {
  let component: Home;
  let fixture: ComponentFixture<Home>;
  let personaService: Partial<PersonaService>;
  let grupoPersonasService: Partial<GrupoPersonasService>;
  let authService: Partial<AuthService>;

  beforeEach(async () => {
    personaService = {
      getPersonas: vi.fn().mockReturnValue(of(dummyPersonas))
    };

    grupoPersonasService = {
      getGrupos: vi.fn().mockReturnValue(of(dummyGrupos))
    };

    authService = {
      isAdmin: vi.fn().mockReturnValue(false)
    };

    await TestBed.configureTestingModule({
      imports: [Home],
      providers: [
        { provide: PersonaService, useValue: personaService },
        { provide: GrupoPersonasService, useValue: grupoPersonasService },
        { provide: AuthService, useValue: authService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(Home);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load personas on init', () => {
    expect(personaService.getPersonas).toHaveBeenCalled();
    expect(component.personas().length).toBe(2);
  });

  it('should load grupos on init', () => {
    expect(grupoPersonasService.getGrupos).toHaveBeenCalled();
    expect(component.grupos().length).toBe(2);
  });

  it('should set first tab active after loading grupos', () => {
    expect(component.tabActivo()).toBe(1);
  });

  it('should show all personas when no filters are applied', () => {
    expect(component.personasFiltradas().length).toBe(2);
  });

  it('should filter personas by nombre', () => {
    component.filtroNombre.set('ana');
    expect(component.personasFiltradas().length).toBe(1);
    expect(component.personasFiltradas()[0].nombre).toBe('Ana');
  });

  it('should filter personas by apellidos', () => {
    component.filtroApellidos.set('mart');
    expect(component.personasFiltradas().length).toBe(1);
    expect(component.personasFiltradas()[0].apellidos).toBe('Martínez');
  });

  it('should return empty array when filter matches nothing', () => {
    component.filtroNombre.set('xyz_no_match');
    expect(component.personasFiltradas().length).toBe(0);
  });

  it('should set errorMsg on persona service failure', () => {
    (personaService.getPersonas as ReturnType<typeof vi.fn>).mockReturnValue(throwError(() => new Error('error')));
    component.cargarPersonas();
    expect(component.errorMsg()).toBeTruthy();
    expect(component.loading()).toBe(false);
  });

  it('should set loading to false after successful load', () => {
    component.cargarPersonas();
    expect(component.loading()).toBe(false);
  });

  it('should set errorMsg on grupos service failure', () => {
    (grupoPersonasService.getGrupos as ReturnType<typeof vi.fn>).mockReturnValue(throwError(() => new Error('error')));
    component.cargarDatos();
    expect(component.errorMsg()).toBeTruthy();
    expect(component.loading()).toBe(false);
  });

  it('grupoActivo should return the current active group', () => {
    component.tabActivo.set(2);
    expect(component.grupoActivo()?.nombre).toBe('Familia');
  });

  it('personasDelTabActivo should filter by active tab', () => {
    component.tabActivo.set(1);
    expect(component.personasDelTabActivo().length).toBe(2);
    component.tabActivo.set(2);
    expect(component.personasDelTabActivo().length).toBe(0);
  });
});



