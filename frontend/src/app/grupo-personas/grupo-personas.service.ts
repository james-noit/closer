import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { GrupoPersonas } from '../models/grupo-personas.model';
import { environment } from '../../environments/environment';

interface GrupoPersonasListResponse {
  _embedded?: {
    grupoPersonasList: GrupoPersonas[];
  };
}

@Injectable({ providedIn: 'root' })
export class GrupoPersonasService {
  private readonly base = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getGrupos(): Observable<GrupoPersonas[]> {
    return this.http.get<GrupoPersonasListResponse>(`${this.base}/grupos-personas`).pipe(
      map(resp => resp._embedded?.grupoPersonasList ?? [])
    );
  }

  createGrupo(grupo: GrupoPersonas): Observable<GrupoPersonas> {
    return this.http.post<GrupoPersonas>(`${this.base}/grupos-personas`, grupo);
  }

  updateGrupo(id: number, grupo: GrupoPersonas): Observable<GrupoPersonas> {
    return this.http.put<GrupoPersonas>(`${this.base}/grupos-personas/${id}`, grupo);
  }

  deleteGrupo(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/grupos-personas/${id}`);
  }
}
