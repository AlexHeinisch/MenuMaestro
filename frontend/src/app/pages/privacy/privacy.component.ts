import { Component } from '@angular/core';
import { SimpleButtonComponent } from '../../components/Button/SimpleButton';
import { WidePageLayoutComponent } from '../../components/Layout/WidePageLayout';
import { PageLayoutComponent } from '../../components/Layout/PageLayout';

@Component({
    selector: 'app-privacy',
    templateUrl: './privacy.component.html',
    imports: [SimpleButtonComponent, PageLayoutComponent]
})
export class PrivacyComponent {}
