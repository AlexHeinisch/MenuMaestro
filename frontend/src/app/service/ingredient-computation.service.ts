import { Injectable } from '@angular/core';
import { IngredientUnitDto } from '../../generated/ingredients';

@Injectable({
  providedIn: 'root',
})
export class IngredientComputationService {
  constructor() {}

  public roundAmountForDisplay(givenAmount: number, givenUnit: IngredientUnitDto): [number, IngredientUnitDto] {
    let amount = givenAmount;
    let unit = givenUnit;

    if (unit === IngredientUnitDto.Grams && amount >= 500) {
      amount = amount / 1000;
      unit = IngredientUnitDto.Kilograms;
    }

    if (unit === IngredientUnitDto.Kilograms && amount < 0.5) {
      amount = amount * 1000;
      unit = IngredientUnitDto.Grams;
    }

    if (unit === IngredientUnitDto.Millilitres && amount >= 500) {
      amount = amount / 1000;
      unit = IngredientUnitDto.Litres;
    }

    if (unit === IngredientUnitDto.Litres && amount < 0.5) {
      amount = amount * 1000;
      unit = IngredientUnitDto.Millilitres;
    }

    if (unit === IngredientUnitDto.Teaspoons && amount >= 3) {
      amount = amount / 3;
      unit = IngredientUnitDto.Tablespoons;
    }

    if (unit === IngredientUnitDto.Tablespoons && amount < 1) {
      amount = amount * 3;
      unit = IngredientUnitDto.Teaspoons;
    }

    if (unit === IngredientUnitDto.Tablespoons && amount >= 16) {
      amount = amount / 16;
      unit = IngredientUnitDto.Cups;
    }

    if (unit === IngredientUnitDto.Cups && amount < 1) {
      amount = amount * 16;
      unit = IngredientUnitDto.Tablespoons;
    }

    if (unit === IngredientUnitDto.Tablespoons && amount < 1) {
      amount = amount * 3;
      unit = IngredientUnitDto.Teaspoons;
    }

    const roundedAmount = this.roundToPrecision(amount, unit);
    return [roundedAmount, unit];
  }

  public roundAmountForDisplayString(givenAmount: number, givenUnit: IngredientUnitDto): string {
    const [amount, unit] = this.roundAmountForDisplay(givenAmount, givenUnit);
    return amount + this.formatUnitDisplay(unit);
  }

  private fractionToRoundToByUnit(unit: IngredientUnitDto): number {
    switch (unit) {
      case IngredientUnitDto.Litres:
        return 100;
      case IngredientUnitDto.Millilitres:
        return 1;
      case IngredientUnitDto.Kilograms:
        return 100;
      case IngredientUnitDto.Grams:
        return 1;
      case IngredientUnitDto.Ounces:
        return 4;
      case IngredientUnitDto.Cups:
        return 4;
      case IngredientUnitDto.Tablespoons:
        return 4;
      case IngredientUnitDto.Teaspoons:
        return 2;
      case IngredientUnitDto.Piece:
        return 4;
      default:
        return 1;
    }
  }

  private roundToPrecision(amount: number, unit: IngredientUnitDto): number {
    const precisionFactor = this.fractionToRoundToByUnit(unit);
    const roundedAmount = Math.max(1 / precisionFactor, Math.round(amount * precisionFactor) / precisionFactor);
    return roundedAmount;
  }

  public formatUnitDisplay(unit: IngredientUnitDto): string {
    switch (unit) {
      case IngredientUnitDto.Litres:
        return 'L';
      case IngredientUnitDto.Millilitres:
        return 'mL';
      case IngredientUnitDto.Kilograms:
        return 'kg';
      case IngredientUnitDto.Grams:
        return 'g';
      case IngredientUnitDto.Ounces:
        return ' ounces';
      case IngredientUnitDto.Cups:
        return ' cups';
      case IngredientUnitDto.Tablespoons:
        return ' tablespoons';
      case IngredientUnitDto.Teaspoons:
        return ' teaspoons';
      case IngredientUnitDto.Piece:
        return ' piece';
      default:
        return '';
    }
  }
}
