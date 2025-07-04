import {Component} from '@angular/core';
import { SimpleButtonComponent } from '../../components/Button/SimpleButton';

@Component({
    selector: 'app-hero',
    templateUrl: './hero.component.html',
    imports: [SimpleButtonComponent]
})
export class HeroComponent { }
