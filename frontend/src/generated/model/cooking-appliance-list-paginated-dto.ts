/**
 * merged spec
 *
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
import { Pageable } from './pageable';
import { Sort } from './sort';
import { CookingApplianceDto } from './cooking-appliance-dto';


/**
 * Paginated list of cooking appliances.
 */
export interface CookingApplianceListPaginatedDto { 
    totalElements: number;
    totalPages: number;
    sort: Sort;
    first: boolean;
    last: boolean;
    number: number;
    pageable: Pageable;
    numberOfElements: number;
    size: number;
    empty: boolean;
    content: Array<CookingApplianceDto>;
}

