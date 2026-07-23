import { Injectable } from '@angular/core';

declare var gtag: (...args: unknown[]) => void;

@Injectable({providedIn: 'root'})
export class AnalyticsService {

    trackEvent(eventName: string, eventDetails: Record<string, unknown>) {
        gtag('event', eventName, eventDetails)
    }

    // TODO:: build some default events as we go to track different clicks
}