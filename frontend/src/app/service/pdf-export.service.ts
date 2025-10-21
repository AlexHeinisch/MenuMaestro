import { Injectable } from '@angular/core';
import jsPDF from 'jspdf';
import html2canvas from 'html2canvas';
import { MealDto, RecipeDto, IngredientUnitDto } from '../../generated';
import { IngredientComputationService } from './ingredient-computation.service';

@Injectable({
  providedIn: 'root',
})
export class PdfExportService {
  constructor(private ingredientComputationService: IngredientComputationService) {}

  /**
   * Export a meal as a PDF document
   */
  async exportMealToPdf(meal: MealDto): Promise<void> {
    const doc = new jsPDF('p', 'mm', 'a4');
    const pageWidth = doc.internal.pageSize.getWidth();
    const pageHeight = doc.internal.pageSize.getHeight();
    const margin = 15;
    const contentWidth = pageWidth - 2 * margin;
    let yPosition = margin;

    // Colors from Tailwind config
    const primaryColor = '#20686C';
    const textColor = '#000000';
    const lightGray = '#E5E7EB';

    // Title
    doc.setFontSize(24);
    doc.setFont('helvetica', 'bold');
    doc.setTextColor(primaryColor);
    doc.text(meal.name || 'Unknown Meal', margin, yPosition);
    yPosition += 10;

    // Author
    doc.setFontSize(10);
    doc.setFont('helvetica', 'normal');
    doc.setTextColor(textColor);
    doc.text(`By ${meal.recipe.author || 'Unknown Author'}`, margin, yPosition);
    yPosition += 8;

    // Status badge (if not done)
    if (meal.status) {
      doc.setFontSize(9);
      doc.setFont('helvetica', 'bold');
      const statusText = this.formatStatus(meal.status);
      doc.text(`Status: ${statusText}`, margin, yPosition);
      yPosition += 8;
    }

    // Add image if available
    if (meal.recipe.imageLink) {
      try {
        const imgData = await this.loadImage(meal.recipe.imageLink);
        const imgWidth = contentWidth;
        const imgHeight = 60;
        doc.addImage(imgData, 'JPEG', margin, yPosition, imgWidth, imgHeight);
        yPosition += imgHeight + 8;
      } catch (error) {
        console.warn('Could not load image for PDF:', error);
      }
    }

    // Check if we need a new page
    if (yPosition > pageHeight - 60) {
      doc.addPage();
      yPosition = margin;
    }

    // Description section
    if (meal.recipe.description) {
      doc.setFontSize(14);
      doc.setFont('helvetica', 'bold');
      doc.setTextColor(primaryColor);
      doc.text('Description', margin, yPosition);
      yPosition += 6;

      doc.setFontSize(10);
      doc.setFont('helvetica', 'normal');
      doc.setTextColor(textColor);
      const descriptionLines = doc.splitTextToSize(meal.recipe.description, contentWidth);
      doc.text(descriptionLines, margin, yPosition);
      yPosition += descriptionLines.length * 5 + 8;
    }

    // Check if we need a new page
    if (yPosition > pageHeight - 80) {
      doc.addPage();
      yPosition = margin;
    }

    // Ingredients section
    doc.setFontSize(14);
    doc.setFont('helvetica', 'bold');
    doc.setTextColor(primaryColor);

    // Draw a line above ingredients
    doc.setDrawColor(0, 0, 0);
    doc.setLineWidth(1);
    doc.line(margin, yPosition - 2, pageWidth - margin, yPosition - 2);
    yPosition += 6;

    doc.text('Ingredients', margin, yPosition);
    yPosition += 6;

    doc.setFontSize(9);
    doc.setFont('helvetica', 'bold');
    doc.text(`Yield: ${meal.numberOfPeople || 'Unknown'} servings`, margin, yPosition);
    yPosition += 5;

    doc.setFont('helvetica', 'normal');
    if (meal.recipe.ingredients && meal.recipe.ingredients.length > 0) {
      for (const ingredient of meal.recipe.ingredients) {
        // Check if we need a new page
        if (yPosition > pageHeight - 20) {
          doc.addPage();
          yPosition = margin;
        }

        const scaledAmount = this.calculateScaledAmount(
          ingredient.amount,
          ingredient.unit,
          meal.numberOfPeople,
          meal.recipe.servings
        );
        doc.text(`• ${scaledAmount} of ${ingredient.name}`, margin + 3, yPosition);
        yPosition += 5;
      }
    } else {
      doc.setFont('helvetica', 'italic');
      doc.text('No ingredients listed', margin + 3, yPosition);
      yPosition += 5;
    }

    yPosition += 5;

    // Check if we need a new page
    if (yPosition > pageHeight - 60) {
      doc.addPage();
      yPosition = margin;
    }

    // Cooking Appliances section
    doc.setFontSize(14);
    doc.setFont('helvetica', 'bold');
    doc.setTextColor(primaryColor);

    // Draw a line above cooking appliances
    doc.setDrawColor(0, 0, 0);
    doc.setLineWidth(1);
    doc.line(margin, yPosition - 2, pageWidth - margin, yPosition - 2);
    yPosition += 6;

    doc.text('Cooking Appliances', margin, yPosition);
    yPosition += 6;

    doc.setFontSize(9);
    doc.setFont('helvetica', 'normal');
    doc.setTextColor(textColor);

    if (meal.recipe.cookingAppliances && meal.recipe.cookingAppliances.length > 0) {
      for (const appliance of meal.recipe.cookingAppliances) {
        // Check if we need a new page
        if (yPosition > pageHeight - 20) {
          doc.addPage();
          yPosition = margin;
        }

        doc.text(`• ${appliance.amount} x ${appliance.name}`, margin + 3, yPosition);
        yPosition += 5;
      }
    } else {
      doc.setFont('helvetica', 'italic');
      doc.text('No cooking appliances needed', margin + 3, yPosition);
    }

    // Add footer
    const footer = 'Generated by MenuMaestro';
    doc.setFontSize(8);
    doc.setFont('helvetica', 'italic');
    doc.setTextColor(150, 150, 150);
    doc.text(footer, pageWidth / 2, pageHeight - 10, { align: 'center' });

    // Save the PDF
    const fileName = `${meal.name || 'meal'}_recipe.pdf`.replace(/[^a-z0-9_-]/gi, '_');
    doc.save(fileName);
  }

  /**
   * Export a recipe as a PDF document
   */
  async exportRecipeToPdf(recipe: RecipeDto): Promise<void> {
    const doc = new jsPDF('p', 'mm', 'a4');
    const pageWidth = doc.internal.pageSize.getWidth();
    const pageHeight = doc.internal.pageSize.getHeight();
    const margin = 15;
    const contentWidth = pageWidth - 2 * margin;
    let yPosition = margin;

    // Colors
    const primaryColor = '#20686C';
    const textColor = '#000000';

    // Title
    doc.setFontSize(24);
    doc.setFont('helvetica', 'bold');
    doc.setTextColor(primaryColor);
    doc.text(recipe.name || 'Unknown Recipe', margin, yPosition);
    yPosition += 10;

    // Author
    doc.setFontSize(10);
    doc.setFont('helvetica', 'normal');
    doc.setTextColor(textColor);
    doc.text(`By ${recipe.author || 'Unknown Author'}`, margin, yPosition);
    yPosition += 10;

    // Add image if available
    if (recipe.imageLink) {
      try {
        const imgData = await this.loadImage(recipe.imageLink);
        const imgWidth = contentWidth;
        const imgHeight = 60;
        doc.addImage(imgData, 'JPEG', margin, yPosition, imgWidth, imgHeight);
        yPosition += imgHeight + 8;
      } catch (error) {
        console.warn('Could not load image for PDF:', error);
      }
    }

    // Check if we need a new page
    if (yPosition > pageHeight - 60) {
      doc.addPage();
      yPosition = margin;
    }

    // Description section
    if (recipe.description) {
      doc.setFontSize(14);
      doc.setFont('helvetica', 'bold');
      doc.setTextColor(primaryColor);
      doc.text('Description', margin, yPosition);
      yPosition += 6;

      doc.setFontSize(10);
      doc.setFont('helvetica', 'normal');
      doc.setTextColor(textColor);
      const descriptionLines = doc.splitTextToSize(recipe.description, contentWidth);
      doc.text(descriptionLines, margin, yPosition);
      yPosition += descriptionLines.length * 5 + 8;
    }

    // Check if we need a new page
    if (yPosition > pageHeight - 80) {
      doc.addPage();
      yPosition = margin;
    }

    // Ingredients section
    doc.setFontSize(14);
    doc.setFont('helvetica', 'bold');
    doc.setTextColor(primaryColor);

    // Draw a line above ingredients
    doc.setDrawColor(0, 0, 0);
    doc.setLineWidth(1);
    doc.line(margin, yPosition - 2, pageWidth - margin, yPosition - 2);
    yPosition += 6;

    doc.text('Ingredients', margin, yPosition);
    yPosition += 6;

    doc.setFontSize(9);
    doc.setFont('helvetica', 'bold');
    doc.text(`Yield: ${recipe.servings || 'Unknown'} servings`, margin, yPosition);
    yPosition += 5;

    doc.setFont('helvetica', 'normal');
    if (recipe.ingredients && recipe.ingredients.length > 0) {
      for (const ingredient of recipe.ingredients) {
        // Check if we need a new page
        if (yPosition > pageHeight - 20) {
          doc.addPage();
          yPosition = margin;
        }

        const unitDisplay = this.ingredientComputationService.formatUnitDisplay(ingredient.unit);
        doc.text(`• ${ingredient.amount}${unitDisplay} of ${ingredient.name}`, margin + 3, yPosition);
        yPosition += 5;
      }
    } else {
      doc.setFont('helvetica', 'italic');
      doc.text('No ingredients listed', margin + 3, yPosition);
      yPosition += 5;
    }

    yPosition += 5;

    // Check if we need a new page
    if (yPosition > pageHeight - 60) {
      doc.addPage();
      yPosition = margin;
    }

    // Cooking Appliances section
    doc.setFontSize(14);
    doc.setFont('helvetica', 'bold');
    doc.setTextColor(primaryColor);

    // Draw a line above cooking appliances
    doc.setDrawColor(0, 0, 0);
    doc.setLineWidth(1);
    doc.line(margin, yPosition - 2, pageWidth - margin, yPosition - 2);
    yPosition += 6;

    doc.text('Cooking Appliances', margin, yPosition);
    yPosition += 6;

    doc.setFontSize(9);
    doc.setFont('helvetica', 'normal');
    doc.setTextColor(textColor);

    if (recipe.cookingAppliances && recipe.cookingAppliances.length > 0) {
      for (const appliance of recipe.cookingAppliances) {
        // Check if we need a new page
        if (yPosition > pageHeight - 20) {
          doc.addPage();
          yPosition = margin;
        }

        doc.text(`• ${appliance.amount} x ${appliance.name}`, margin + 3, yPosition);
        yPosition += 5;
      }
    } else {
      doc.setFont('helvetica', 'italic');
      doc.text('No cooking appliances needed', margin + 3, yPosition);
    }

    // Add footer
    const footer = 'Generated by MenuMaestro';
    doc.setFontSize(8);
    doc.setFont('helvetica', 'italic');
    doc.setTextColor(150, 150, 150);
    doc.text(footer, pageWidth / 2, pageHeight - 10, { align: 'center' });

    // Save the PDF
    const fileName = `${recipe.name || 'recipe'}.pdf`.replace(/[^a-z0-9_-]/gi, '_');
    doc.save(fileName);
  }

  /**
   * Load an image from a URL and convert it to base64
   */
  private async loadImage(url: string): Promise<string> {
    return new Promise((resolve, reject) => {
      const img = new Image();
      img.crossOrigin = 'Anonymous';
      img.onload = () => {
        const canvas = document.createElement('canvas');
        canvas.width = img.width;
        canvas.height = img.height;
        const ctx = canvas.getContext('2d');
        if (ctx) {
          ctx.drawImage(img, 0, 0);
          resolve(canvas.toDataURL('image/jpeg', 0.8));
        } else {
          reject(new Error('Could not get canvas context'));
        }
      };
      img.onerror = () => reject(new Error('Could not load image'));
      img.src = url;
    });
  }

  /**
   * Calculate scaled ingredient amount for meals
   */
  private calculateScaledAmount(
    ingredientAmount: number,
    unit: IngredientUnitDto,
    mealServings: number,
    recipeServings: number
  ): string {
    if (mealServings && recipeServings) {
      const scaledAmount = ingredientAmount * (mealServings / recipeServings);
      return this.ingredientComputationService.roundAmountForDisplayString(scaledAmount, unit);
    }
    return ingredientAmount + this.ingredientComputationService.formatUnitDisplay(unit);
  }

  /**
   * Format meal status for display
   */
  private formatStatus(status: string): string {
    if (!status) return 'Unknown Status';
    return status
      .toLowerCase()
      .replace(/_/g, ' ')
      .replace(/\b\w/g, (char) => char.toUpperCase());
  }
}
