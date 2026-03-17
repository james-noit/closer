import { GrupoPersonas } from './grupo-personas.model';

export interface Contacto {
  id?: number;
  nombre: string;
  apellidos: string;
  numeroTelefono: string;
  fechaCumpleanos: string;
  email?: string;
  grupoPersonas?: GrupoPersonas;
  usuarioId?: number;
}
