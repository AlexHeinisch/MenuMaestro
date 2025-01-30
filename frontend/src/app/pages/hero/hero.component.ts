import { Component } from '@angular/core';
import { SimpleButtonComponent } from '../../components/Button/SimpleButton';

@Component({
  standalone: true,
  selector: 'app-hero',
  templateUrl: './hero.component.html',
  imports: [SimpleButtonComponent],
})
export class HeroComponent {}
