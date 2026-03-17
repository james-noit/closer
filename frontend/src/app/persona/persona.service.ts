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
}
