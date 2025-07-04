import { Injectable } from '@angular/core';

declare var gtag : any;

@Injectable({providedIn: 'root'})
export class AnalyticsService {

    trackEvent(eventName: string, eventDetails: any) {
        gtag('event', eventName, eventDetails)
    }

    // TODO:: build some default events as we go to track different clicks
}