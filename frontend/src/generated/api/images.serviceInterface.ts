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
import { ImageUploadResponseDto } from '../model/models';


import { Configuration }                                     from '../configuration';



export interface ImagesApiServiceInterface {
    defaultHeaders: HttpHeaders;
    configuration: Configuration;

    /**
     * 
     * Download an image
     * @param id 
     */
    downloadImage(id: string, extraHttpRequestParams?: any): Observable<Blob>;

    /**
     * 
     * Upload an image file, get back an identifier to refer to the record.
     * @param file 
     */
    uploadImage(file?: Blob, extraHttpRequestParams?: any): Observable<ImageUploadResponseDto>;

}
