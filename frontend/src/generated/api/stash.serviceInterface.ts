/**
 * merged spec
 *
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
import { HttpHeaders }                                       from '@angular/common/http';

import { Observable }                                        from 'rxjs';

import { ErrorResponse } from '../model/models';
import { IngredientUseCreateEditDto } from '../model/models';
import { StashResponseDto } from '../model/models';
import { StashSearchResponseDto } from '../model/models';


import { Configuration }                                     from '../configuration';



export interface StashApiServiceInterface {
    defaultHeaders: HttpHeaders;
    configuration: Configuration;

    /**
     * 
     * 
     * @param id 
     */
    getStash(id: number, extraHttpRequestParams?: any): Observable<StashResponseDto>;

    /**
     * 
     * Removes the selected ingredient amounts from one stash and adds them to another.
     * @param id 
     * @param otherStashId 
     * @param ingredientUseCreateEditDto Specifies which ingredients + amounts to move
     */
    moveStashIngredients(id: number, otherStashId: number, ingredientUseCreateEditDto: Array<IngredientUseCreateEditDto>, extraHttpRequestParams?: any): Observable<{}>;

    /**
     * 
     * 
     * @param name 
     * @param page 
     * @param size 
     */
    searchStashes(name?: string, page?: number, size?: number, extraHttpRequestParams?: any): Observable<Array<StashSearchResponseDto>>;

    /**
     * 
     * 
     * @param id 
     * @param ingredientUseCreateEditDto List of ingredients to modify. Use amount 0 to delete, nonzero amount to add
     * @param ifMatch Standard precondition header, give an ETAG here. See the 412 response status.
     */
    updateStashIngredients(id: number, ingredientUseCreateEditDto: Array<IngredientUseCreateEditDto>, ifMatch?: string, extraHttpRequestParams?: any): Observable<{}>;

}
