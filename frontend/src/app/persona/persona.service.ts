import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Contacto } from '../models/contacto.model';
import { environment } from '../../environments/environment';

interface PersonaListResponse {
  _embedded?: {
    personaList: Contacto[];
  };
}

@Injectable({ providedIn: 'root' })
export class PersonaService {
  private readonly base = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getPersonas(): Observable<Contacto[]> {
    return this.http.get<PersonaListResponse>(`${this.base}/personas`).pipe(
      map(resp => resp._embedded?.personaList ?? [])
    );
  }

  createPersona(persona: Contacto): Observable<Contacto> {
    return this.http.post<Contacto>(`${this.base}/personas`, persona);
  }

  updatePersona(id: number, persona: Contacto): Observable<Contacto> {
    return this.http.put<Contacto>(`${this.base}/personas/${id}`, persona);
  }

  deletePersona(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/personas/${id}`);
  }

  asignarUsuario(personaId: number, usuarioId: number): Observable<Contacto> {
    return this.http.put<Contacto>(`${this.base}/personas/${personaId}/asignar-usuario/${usuarioId}`, {});
  }
}
