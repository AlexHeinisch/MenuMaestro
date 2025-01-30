import { Component, OnInit } from '@angular/core';
import { EditRecipeComponent } from '../recipe-edit/recipe-edit.component';
import { ActivatedRoute } from '@angular/router';
import { NgIf } from '@angular/common';
import { RecipesApiService } from '../../../../generated/recipes/api/recipes.service';
import { RecipeCreateEditDto, RecipeDto } from '../../../../generated/recipes';
import { Observable, tap } from 'rxjs';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-recipe-edit-page',
  standalone: true,
  imports: [EditRecipeComponent, NgIf],
  templateUrl: './recipe-edit-page.component.html',
})
export class RecipeEditPageComponent implements OnInit {
  recipeId: number | undefined;

  constructor(
    private route: ActivatedRoute,
    private toastr: ToastrService,
    private recipesApiService: RecipesApiService
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe((params) => {
      this.recipeId = +params['id'];
    });
  }

  get redirectPath(): string {
    return `/recipes/${this.recipeId}`;
  }

  fetchRecipeValueHandler(): Observable<RecipeDto> {
    return this.recipesApiService.getRecipeById(this.recipeId!);
  }

  editRecipeHandler(recipeEdit: RecipeCreateEditDto): Observable<any> {
    return this.recipesApiService
      .editRecipeById(this.recipeId!, recipeEdit)
      .pipe(tap((next) => this.toastr.success('Recipe updated.')));
  }
}
