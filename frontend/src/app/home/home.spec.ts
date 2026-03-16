import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';

import { Home } from './home';
import { PersonaService } from '../persona/persona.service';
import { Contacto } from '../models/contacto.model';

const dummyPersonas: Contacto[] = [
  { id: 1, nombre: 'Ana', apellidos: 'García', numeroTelefono: '+34600000001', fechaCumpleanos: '1990-03-15', email: 'ana@example.com' },
  { id: 2, nombre: 'Luis', apellidos: 'Martínez', numeroTelefono: '+34600000002', fechaCumpleanos: '1985-07-22' }
];

describe('Home', () => {
  let component: Home;
  let fixture: ComponentFixture<Home>;
  let personaService: Partial<PersonaService>;

  beforeEach(async () => {
    personaService = {
      getPersonas: vi.fn().mockReturnValue(of(dummyPersonas))
    };

    await TestBed.configureTestingModule({
      imports: [Home],
      providers: [{ provide: PersonaService, useValue: personaService }]
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

  it('should set errorMsg on service failure', () => {
    (personaService.getPersonas as ReturnType<typeof vi.fn>).mockReturnValue(throwError(() => new Error('error')));
    component.cargarPersonas();
    expect(component.errorMsg()).toBeTruthy();
    expect(component.loading()).toBe(false);
  });

  it('should set loading to false after successful load', () => {
    component.cargarPersonas();
    expect(component.loading()).toBe(false);
  });
});



