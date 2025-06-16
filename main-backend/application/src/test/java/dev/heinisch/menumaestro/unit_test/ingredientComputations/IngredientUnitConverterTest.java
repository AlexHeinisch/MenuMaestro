package dev.heinisch.menumaestro.unit_test.ingredientComputations;

import dev.heinisch.menumaestro.domain.ingredient.IngredientUnit;
import dev.heinisch.menumaestro.domain.ingredient_computation.IngredientUnitConversionService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class IngredientUnitConverterTest {
    private IngredientUnitConversionService converter;
    @BeforeEach
    public void setUp() {
        converter = new IngredientUnitConversionService();
    }
    @Test
    public void testConvertAmountTo_SameUnit() {
        Assertions.assertEquals(100, converter.convertAmountTo(100, IngredientUnit.TABLESPOONS, IngredientUnit.TABLESPOONS));
    }

    @Test
    public void testConvertFromMillilitresToLitres() {
        Assertions.assertEquals(1, converter.convertAmountTo(1000, IngredientUnit.MILLILITRES, IngredientUnit.LITRES));
    }

    @Test
    public void testConvertFromMillilitresToCups() {
        Assertions.assertEquals(1, converter.convertAmountTo(236, IngredientUnit.MILLILITRES, IngredientUnit.CUPS));
    }

    @Test
    public void testConvertFromMillilitresToTablespoons() {
        Assertions.assertEquals(1, converter.convertAmountTo(15, IngredientUnit.MILLILITRES, IngredientUnit.TABLESPOONS));
    }

    @Test
    public void testConvertFromMillilitresToTeaspoons() {
        Assertions.assertEquals(1, converter.convertAmountTo(5, IngredientUnit.MILLILITRES, IngredientUnit.TEASPOONS));
    }

    @Test
    public void testConvertFromLitresToMillilitres() {
        Assertions.assertEquals(1000, converter.convertAmountTo(1, IngredientUnit.LITRES, IngredientUnit.MILLILITRES));
    }

    @Test
    public void testConvertFromLitresToCups() {
        Assertions.assertEquals(4, converter.convertAmountTo(1, IngredientUnit.LITRES, IngredientUnit.CUPS));
    }

    @Test
    public void testConvertFromLitresToTablespoons() {
        Assertions.assertEquals(68, converter.convertAmountTo(1, IngredientUnit.LITRES, IngredientUnit.TABLESPOONS));
    }

    @Test
    public void testConvertFromLitresToTeaspoons() {
        Assertions.assertEquals(202, converter.convertAmountTo(1, IngredientUnit.LITRES, IngredientUnit.TEASPOONS));
    }

    @Test
    public void testConvertFromCupsToMillilitres() {
        Assertions.assertEquals(236, converter.convertAmountTo(1, IngredientUnit.CUPS, IngredientUnit.MILLILITRES));
    }

    @Test
    public void testConvertFromCupsToLitres() {
        Assertions.assertEquals(0.25, converter.convertAmountTo(1, IngredientUnit.CUPS, IngredientUnit.LITRES));
    }

    @Test
    public void testConvertFromCupsToTablespoons() {
        Assertions.assertEquals(16, converter.convertAmountTo(1, IngredientUnit.CUPS, IngredientUnit.TABLESPOONS));
    }

    @Test
    public void testConvertFromCupsToTeaspoons() {
        Assertions.assertEquals(48, converter.convertAmountTo(1, IngredientUnit.CUPS, IngredientUnit.TEASPOONS));
    }

    @Test
    public void testConvertFromTablespoonsToMillilitres() {
        Assertions.assertEquals(15, converter.convertAmountTo(1, IngredientUnit.TABLESPOONS, IngredientUnit.MILLILITRES));
    }

    @Test
    public void testConvertFromTablespoonsToLitres() {
        Assertions.assertEquals(0.014706, converter.convertAmountTo(1, IngredientUnit.TABLESPOONS, IngredientUnit.LITRES), 0.0001);
    }

    @Test
    public void testConvertFromTablespoonsToCups() {
        Assertions.assertEquals(0.0625, converter.convertAmountTo(1, IngredientUnit.TABLESPOONS, IngredientUnit.CUPS), 0.0001);
    }

    @Test
    public void testConvertFromTablespoonsToTeaspoons() {
        Assertions.assertEquals(3, converter.convertAmountTo(1, IngredientUnit.TABLESPOONS, IngredientUnit.TEASPOONS));
    }

    @Test
    public void testConvertFromTeaspoonsToMillilitres() {
        Assertions.assertEquals(5, converter.convertAmountTo(1, IngredientUnit.TEASPOONS, IngredientUnit.MILLILITRES));
    }

    @Test
    public void testConvertFromTeaspoonsToLitres() {
        Assertions.assertEquals(0.004941, converter.convertAmountTo(1, IngredientUnit.TEASPOONS, IngredientUnit.LITRES), 0.0001);
    }

    @Test
    public void testConvertFromTeaspoonsToCups() {
        Assertions.assertEquals(0.020833, converter.convertAmountTo(1, IngredientUnit.TEASPOONS, IngredientUnit.CUPS), 0.0001);
    }

    @Test
    public void testConvertFromTeaspoonsToTablespoons() {
        Assertions.assertEquals(0.333333, converter.convertAmountTo(1, IngredientUnit.TEASPOONS, IngredientUnit.TABLESPOONS), 0.0001);
    }

    @Test
    public void testConvertFromGramsToKilograms() {
        Assertions.assertEquals(1, converter.convertAmountTo(1000, IngredientUnit.GRAMS, IngredientUnit.KILOGRAMS));
    }

    @Test
    public void testConvertFromGramsToOunces() {
        Assertions.assertEquals(1, converter.convertAmountTo(28, IngredientUnit.GRAMS, IngredientUnit.OUNCES));
    }

    @Test
    public void testConvertFromKilogramsToGrams() {
        Assertions.assertEquals(1000, converter.convertAmountTo(1, IngredientUnit.KILOGRAMS, IngredientUnit.GRAMS));
    }

    @Test
    public void testConvertFromKilogramsToOunces() {
        Assertions.assertEquals(35, converter.convertAmountTo(1, IngredientUnit.KILOGRAMS, IngredientUnit.OUNCES));
    }

    @Test
    public void testConvertFromOuncesToGrams() {
        Assertions.assertEquals(28, converter.convertAmountTo(1, IngredientUnit.OUNCES, IngredientUnit.GRAMS));
    }

    @Test
    public void testConvertFromOuncesToKilograms() {
        Assertions.assertEquals(0.0285714, converter.convertAmountTo(1, IngredientUnit.OUNCES, IngredientUnit.KILOGRAMS), 0.0001);
    }

    @Test
    public void testCanConvertSameUnits() {
        Assertions.assertTrue(converter.canConvert(IngredientUnit.TABLESPOONS, IngredientUnit.TABLESPOONS));
    }

    @Test
    public void testCanConvertVolumeUnits() {
        Assertions.assertTrue(converter.canConvert(IngredientUnit.LITRES, IngredientUnit.CUPS));
    }

    @Test
    public void testCannotConvertPiece() {
        Assertions.assertFalse(converter.canConvert(IngredientUnit.PIECE, IngredientUnit.LITRES));
        Assertions.assertFalse(converter.canConvert(IngredientUnit.LITRES, IngredientUnit.PIECE));
    }

    @Test
    public void testCanConvertWeightUnits() {
        Assertions.assertTrue(converter.canConvert(IngredientUnit.GRAMS, IngredientUnit.OUNCES));
    }
}
