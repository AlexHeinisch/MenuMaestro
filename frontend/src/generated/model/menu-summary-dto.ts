/**
 * merged spec
 *
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
import { MenuStatus } from './menu-status';
import { OrganizationSummaryDto } from './organization-summary-dto';


export interface MenuSummaryDto { 
    id: number;
    /**
     * Name of the menu
     */
    name: string;
    organization: OrganizationSummaryDto;
    status: MenuStatus;
}



