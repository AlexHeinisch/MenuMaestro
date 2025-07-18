/**
 * merged spec
 *
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


export interface MenuCreateDto { 
    /**
     * Name of the menu
     */
    name: string;
    /**
     * Description of the menu
     */
    description: string;
    /**
     * Organization for which the menu is planned
     */
    organizationId: number;
    /**
     * Number of people that have the menu
     */
    numberOfPeople: number;
}

