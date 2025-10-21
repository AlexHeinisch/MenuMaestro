package dev.heinisch.menumaestro.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.List;
import com.itextpdf.layout.element.ListItem;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.CookingApplianceUseDto;
import org.openapitools.model.IngredientUseDto;
import org.openapitools.model.MealDto;
import org.openapitools.model.RecipeDto;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Service for generating PDF documents for recipes and meals.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PdfGenerationService {

    /**
     * Generates a PDF document for a recipe.
     *
     * @param recipe the recipe to generate PDF for
     * @return byte array containing the PDF document
     */
    public byte[] generateRecipePdf(RecipeDto recipe) {
        log.info("Generating PDF for recipe: {}", recipe.getName());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Set up fonts
            PdfFont boldFont = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD);
            PdfFont regularFont = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA);

            // Title
            Paragraph title = new Paragraph(recipe.getName())
                    .setFont(boldFont)
                    .setFontSize(24)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(title);

            // Author
            if (recipe.getAuthor() != null) {
                Paragraph author = new Paragraph("By: " + recipe.getAuthor())
                        .setFont(regularFont)
                        .setFontSize(12)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setFontColor(ColorConstants.GRAY)
                        .setMarginBottom(20);
                document.add(author);
            }

            // Servings
            if (recipe.getServings() != null) {
                Paragraph servings = new Paragraph("Servings: " + recipe.getServings())
                        .setFont(regularFont)
                        .setFontSize(12)
                        .setMarginBottom(20);
                document.add(servings);
            }

            // Description
            if (recipe.getDescription() != null && !recipe.getDescription().isEmpty()) {
                Paragraph descriptionHeader = new Paragraph("Description")
                        .setFont(boldFont)
                        .setFontSize(16)
                        .setMarginBottom(10);
                document.add(descriptionHeader);

                Paragraph description = new Paragraph(recipe.getDescription())
                        .setFont(regularFont)
                        .setFontSize(12)
                        .setMarginBottom(20);
                document.add(description);
            }

            // Ingredients
            if (recipe.getIngredients() != null && !recipe.getIngredients().isEmpty()) {
                Paragraph ingredientsHeader = new Paragraph("Ingredients")
                        .setFont(boldFont)
                        .setFontSize(16)
                        .setMarginBottom(10);
                document.add(ingredientsHeader);

                List ingredientsList = new List()
                        .setFont(regularFont)
                        .setFontSize(12)
                        .setMarginBottom(20);

                for (IngredientUseDto ingredient : recipe.getIngredients()) {
                    String ingredientText = formatIngredient(ingredient);
                    ingredientsList.add(new ListItem(ingredientText));
                }

                document.add(ingredientsList);
            }

            // Cooking Appliances
            if (recipe.getCookingAppliances() != null && !recipe.getCookingAppliances().isEmpty()) {
                Paragraph appliancesHeader = new Paragraph("Cooking Appliances")
                        .setFont(boldFont)
                        .setFontSize(16)
                        .setMarginBottom(10);
                document.add(appliancesHeader);

                List appliancesList = new List()
                        .setFont(regularFont)
                        .setFontSize(12)
                        .setMarginBottom(20);

                for (CookingApplianceUseDto appliance : recipe.getCookingAppliances()) {
                    String applianceText = formatAppliance(appliance);
                    appliancesList.add(new ListItem(applianceText));
                }

                document.add(appliancesList);
            }

            // Footer
            Paragraph footer = new Paragraph("Generated by MenuMaestro")
                    .setFont(regularFont)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.GRAY)
                    .setMarginTop(30);
            document.add(footer);

            document.close();

            log.info("Successfully generated PDF for recipe: {}", recipe.getName());
            return baos.toByteArray();

        } catch (IOException e) {
            log.error("Error generating PDF for recipe: {}", recipe.getName(), e);
            throw new RuntimeException("Failed to generate PDF for recipe", e);
        }
    }

    /**
     * Generates a PDF document for a meal.
     *
     * @param meal the meal to generate PDF for
     * @return byte array containing the PDF document
     */
    public byte[] generateMealPdf(MealDto meal) {
        log.info("Generating PDF for meal: {}", meal.getName());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Set up fonts
            PdfFont boldFont = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD);
            PdfFont regularFont = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA);

            // Title
            Paragraph title = new Paragraph(meal.getName())
                    .setFont(boldFont)
                    .setFontSize(24)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(title);

            // Author (from recipe)
            if (meal.getRecipe() != null && meal.getRecipe().getAuthor() != null) {
                Paragraph author = new Paragraph("By: " + meal.getRecipe().getAuthor())
                        .setFont(regularFont)
                        .setFontSize(12)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setFontColor(ColorConstants.GRAY)
                        .setMarginBottom(20);
                document.add(author);
            }

            // Number of People
            if (meal.getNumberOfPeople() != null) {
                Paragraph servings = new Paragraph("Servings: " + meal.getNumberOfPeople())
                        .setFont(regularFont)
                        .setFontSize(12)
                        .setMarginBottom(20);
                document.add(servings);
            }

            // Description (from recipe)
            if (meal.getRecipe() != null && meal.getRecipe().getDescription() != null
                    && !meal.getRecipe().getDescription().isEmpty()) {
                Paragraph descriptionHeader = new Paragraph("Description")
                        .setFont(boldFont)
                        .setFontSize(16)
                        .setMarginBottom(10);
                document.add(descriptionHeader);

                Paragraph description = new Paragraph(meal.getRecipe().getDescription())
                        .setFont(regularFont)
                        .setFontSize(12)
                        .setMarginBottom(20);
                document.add(description);
            }

            // Scaled Ingredients
            if (meal.getRecipe() != null && meal.getRecipe().getIngredients() != null
                    && !meal.getRecipe().getIngredients().isEmpty()) {
                Paragraph ingredientsHeader = new Paragraph("Ingredients (Scaled)")
                        .setFont(boldFont)
                        .setFontSize(16)
                        .setMarginBottom(10);
                document.add(ingredientsHeader);

                List ingredientsList = new List()
                        .setFont(regularFont)
                        .setFontSize(12)
                        .setMarginBottom(20);

                for (IngredientUseDto ingredient : meal.getRecipe().getIngredients()) {
                    String ingredientText = formatIngredient(ingredient);
                    ingredientsList.add(new ListItem(ingredientText));
                }

                document.add(ingredientsList);
            }

            // Cooking Appliances
            if (meal.getRecipe() != null && meal.getRecipe().getCookingAppliances() != null
                    && !meal.getRecipe().getCookingAppliances().isEmpty()) {
                Paragraph appliancesHeader = new Paragraph("Cooking Appliances")
                        .setFont(boldFont)
                        .setFontSize(16)
                        .setMarginBottom(10);
                document.add(appliancesHeader);

                List appliancesList = new List()
                        .setFont(regularFont)
                        .setFontSize(12)
                        .setMarginBottom(20);

                for (CookingApplianceUseDto appliance : meal.getRecipe().getCookingAppliances()) {
                    String applianceText = formatAppliance(appliance);
                    appliancesList.add(new ListItem(applianceText));
                }

                document.add(appliancesList);
            }

            // Footer
            Paragraph footer = new Paragraph("Generated by MenuMaestro")
                    .setFont(regularFont)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.GRAY)
                    .setMarginTop(30);
            document.add(footer);

            document.close();

            log.info("Successfully generated PDF for meal: {}", meal.getName());
            return baos.toByteArray();

        } catch (IOException e) {
            log.error("Error generating PDF for meal: {}", meal.getName(), e);
            throw new RuntimeException("Failed to generate PDF for meal", e);
        }
    }

    /**
     * Formats an ingredient for display in the PDF.
     */
    private String formatIngredient(IngredientUseDto ingredient) {
        StringBuilder sb = new StringBuilder();

        if (ingredient.getAmount() != null) {
            sb.append(String.format("%.2f", ingredient.getAmount())).append(" ");
        }

        if (ingredient.getUnit() != null) {
            sb.append(ingredient.getUnit().getValue()).append(" ");
        }

        if (ingredient.getIngredient() != null && ingredient.getIngredient().getName() != null) {
            sb.append(ingredient.getIngredient().getName());
        }

        return sb.toString().trim();
    }

    /**
     * Formats a cooking appliance for display in the PDF.
     */
    private String formatAppliance(CookingApplianceUseDto appliance) {
        StringBuilder sb = new StringBuilder();

        if (appliance.getAmount() != null && appliance.getAmount() > 1) {
            sb.append(appliance.getAmount()).append("x ");
        }

        if (appliance.getCookingAppliance() != null && appliance.getCookingAppliance().getName() != null) {
            sb.append(appliance.getCookingAppliance().getName());
        }

        return sb.toString().trim();
    }
}
