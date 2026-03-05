import { Component } from '@angular/core';

interface GraficoColumna {
  etiqueta: string;
  valor: number;
  claseCalor: string;
}

@Component({
  selector: 'app-home',
  imports: [],
  templateUrl: './home.html',
  styleUrl: './home.scss'
})
export class Home {
  readonly columnas: GraficoColumna[] = [
    { etiqueta: 'Lun', valor: 34, claseCalor: 'heat-1' },
    { etiqueta: 'Mar', valor: 48, claseCalor: 'heat-2' },
    { etiqueta: 'Mie', valor: 61, claseCalor: 'heat-3' },
    { etiqueta: 'Jue', valor: 74, claseCalor: 'heat-4' },
    { etiqueta: 'Vie', valor: 88, claseCalor: 'heat-5' }
  ];
}
