import { Component } from '@angular/core';
import {
  CdkDragDrop,
  moveItemInArray,
  transferArrayItem,
  CdkDrag,
  CdkDropList,
  CdkDropListGroup,
} from '@angular/cdk/drag-drop';
import { CommonModule } from '@angular/common';
import { SimpleButtonComponent, ButtonVariant } from '../../../../components/Button/SimpleButton';
import { FormsModule } from '@angular/forms';

@Component({
    selector: 'app-list-drag-and-drop',
    imports: [CommonModule, CdkDropList, CdkDrag, SimpleButtonComponent, CdkDropListGroup, FormsModule],
    templateUrl: './list-drag-and-drop.component.html'
})
export class ListDragAndDropComponent {
  ButtonVariant = ButtonVariant;

  newListName: string = '';
  newShoppingListItem: string = '';

  lists = [
    { id: 1, name: 'Planned', items: [{ id: 9, name: "Lunch: Caesar's Salad" }], collapsed: false }, // New list added first
    {
      id: 2,
      name: 'Day 1 Meal Plan',
      items: [
        { id: 1, name: 'Day 1 Breakfast: Pancakes' },
        { id: 2, name: 'Day 1 Lunch: Salad' },
      ],
      collapsed: false,
    },
    { id: 3, name: 'Day 2 Meal Plan', items: [{ id: 3, name: 'Day 2 Dinner: Spaghetti' }], collapsed: false },
    {
      id: 4,
      name: 'Shop For',
      items: [
        { id: 4, name: 'Dinner: Pasta Bolognese' },
        { id: 5, name: 'Lunch: Soup' },
      ],
      collapsed: false,
    },
    { id: 5, name: 'Shopping List Created', items: [{ id: 6, name: 'Breakfast: Cereal' }], collapsed: false },
    { id: 6, name: 'Ready To Cook', items: [{ id: 7, name: 'Lunch: Meatballs' }], collapsed: false },
    { id: 7, name: 'Cooked', items: [{ id: 8, name: 'Dinner: Pizza' }], collapsed: false },
  ];

  // Handle drag-and-drop functionality
  drop(event: CdkDragDrop<{ id: number; name: string }[]>, listIndex: number) {
    const targetList = this.lists[listIndex];

    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
    } else {
      transferArrayItem(event.previousContainer.data, event.container.data, event.previousIndex, event.currentIndex);
    }

    const currentName = event.container.data[event.currentIndex].name;

    // Update the item name if dropped into a list containing 'Day'
    if (targetList.name.includes('Day')) {
      // Extract the 'Day X' number from the target list name (e.g., 'Day 2')
      const targetDayPrefix = targetList.name.split(' ')[0] + ' ' + targetList.name.split(' ')[1]; // 'Day 2'

      // If the item's name already contains 'Day X', replace it with the new prefix
      if (currentName.match(/Day \d+/)) {
        event.container.data[event.currentIndex].name = currentName.replace(/Day \d+/, targetDayPrefix);
      } else {
        // If the item doesn't contain 'Day X', prepend the new 'Day X' prefix
        event.container.data[event.currentIndex].name = targetDayPrefix + ' ' + currentName;
      }
    } else if (targetList.name === 'Planned') {
      // If the target list is 'Planned', remove any 'Day X' prefix
      if (currentName.match(/Day \d+/)) {
        event.container.data[event.currentIndex].name = currentName.replace(/Day \d+/, '').trim();
      }
    }
  }

  moveAllItemsToNextList(index: number) {
    if (index < this.lists.length - 1) {
      const currentList = this.lists[index];
      const nextList = this.lists[index + 1];

      // Transfer all items from current list to next list
      nextList.items.push(...currentList.items);
      currentList.items = [];
    }
  }

  onEdit(item: { id: number; name: string }, list: any) {
    console.log('Edit clicked for:', item, 'in list:', list.name);
  }

  onDetails(item: { id: number; name: string }, list: any) {
    console.log('Details clicked for:', item, 'in list:', list.name);
  }

  onDelete(item: { id: number; name: string }, list: any) {
    console.log('Delete clicked for:', item, 'in list:', list.name);
  }

  toggleCollapse(list: any) {
    list.collapsed = !list.collapsed;
  }

  addNewList() {
    if (this.newListName.trim()) {
      const newList = {
        id: this.lists.length + 1,
        name: this.newListName,
        items: [],
        collapsed: false,
      };
      this.lists.push(newList);
      this.newListName = ''; // Reset the input field
    }
  }

  generateShoppingList(list: any) {
    if (this.newShoppingListItem) {
      this.moveAllItemsToNextList(this.lists.indexOf(list));
      this.newShoppingListItem = '';
    }
  }

  isEmpty(list: any) {
    return list.items.length === 0;
  }
}
