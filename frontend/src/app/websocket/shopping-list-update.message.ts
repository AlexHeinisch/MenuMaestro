import { IngredientCategory, IngredientUnitDto } from '../../generated/shopping-lists';

export interface ShoppingListUpdateMessage {
  updateType: ShoppingListUpdateType; // This is required
  shoppingListItemId?: number | null;
  customName?: string | null;
  name?: string | null;
  unit?: IngredientUnitDto | null;
  amount?: number | null;
  isChecked?: boolean | null;
  checkedBy?: string | null;
  category?: IngredientCategory | null;
}

export enum ShoppingListUpdateType {
  MODIFY = 'MODIFY',
  RELOAD = 'RELOAD',
  CLOSED = 'CLOSED',
}
