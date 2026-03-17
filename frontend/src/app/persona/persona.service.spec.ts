import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { PersonaService } from './persona.service';
import { environment } from '../../environments/environment';
import { Contacto } from '../models/contacto.model';

const dummyPersonas: Contacto[] = [
  { id: 1, nombre: 'Ana', apellidos: 'García', numeroTelefono: '+34600000001', fechaCumpleanos: '1990-03-15', email: 'ana@example.com' },
  { id: 2, nombre: 'Luis', apellidos: 'Martínez', numeroTelefono: '+34600000002', fechaCumpleanos: '1985-07-22' }
];

describe('PersonaService', () => {
  let service: PersonaService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [PersonaService]
    });
    service = TestBed.inject(PersonaService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getPersonas should call correct URL', () => {
    service.getPersonas().subscribe();
    const req = http.expectOne(`${environment.apiUrl}/personas`);
    expect(req.request.method).toBe('GET');
    req.flush({ _embedded: { personaList: dummyPersonas } });
  });

  it('getPersonas should return the personaList from _embedded', () => {
    service.getPersonas().subscribe(personas => {
      expect(personas.length).toBe(2);
      expect(personas[0].nombre).toBe('Ana');
      expect(personas[1].nombre).toBe('Luis');
    });
    const req = http.expectOne(`${environment.apiUrl}/personas`);
    req.flush({ _embedded: { personaList: dummyPersonas } });
  });

  it('getPersonas should return an empty array when _embedded is absent', () => {
    service.getPersonas().subscribe(personas => {
      expect(personas).toEqual([]);
    });
    const req = http.expectOne(`${environment.apiUrl}/personas`);
    req.flush({});
  });
});
