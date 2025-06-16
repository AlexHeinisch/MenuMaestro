package dev.heinisch.menumaestro.domain.ingredient_computation;

import dev.heinisch.menumaestro.domain.ingredient.IngredientUnit;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.heinisch.menumaestro.domain.ingredient.IngredientUnit.CUPS;
import static dev.heinisch.menumaestro.domain.ingredient.IngredientUnit.GRAMS;
import static dev.heinisch.menumaestro.domain.ingredient.IngredientUnit.KILOGRAMS;
import static dev.heinisch.menumaestro.domain.ingredient.IngredientUnit.LITRES;
import static dev.heinisch.menumaestro.domain.ingredient.IngredientUnit.MILLILITRES;
import static dev.heinisch.menumaestro.domain.ingredient.IngredientUnit.OUNCES;
import static dev.heinisch.menumaestro.domain.ingredient.IngredientUnit.PIECE;
import static dev.heinisch.menumaestro.domain.ingredient.IngredientUnit.TABLESPOONS;
import static dev.heinisch.menumaestro.domain.ingredient.IngredientUnit.TEASPOONS;

@Service
public class IngredientUnitConversionService {
    private final Map<IngredientUnit, UnitKind> unitKinds = Map.of(
            LITRES, UnitKind.VOLUME,
            MILLILITRES, UnitKind.VOLUME,
            TABLESPOONS, UnitKind.VOLUME,
            TEASPOONS, UnitKind.VOLUME,
            CUPS, UnitKind.VOLUME,
            KILOGRAMS, UnitKind.MASS,
            GRAMS, UnitKind.MASS,
            OUNCES, UnitKind.MASS,
            PIECE, UnitKind.PIECE
    );

    public Map<Long, List<IngredientUnit>> unitsOfIngredients(List<IngredientUse> ingredients) {
        return ingredients.stream()
                .collect(Collectors.groupingBy((IngredientUse i) -> i.ingredient().getId(),
                        Collectors.mapping(IngredientUse::unit, Collectors.toList())));
    }

    private enum UnitKind {
        MASS,
        VOLUME,
        PIECE
    }

    /*
    Kinds of units:
        volume
        mass
        piece
    Ignoring density to convert from one to another
    */
    public double convertAmountTo(double amount, IngredientUnit fromUnit, IngredientUnit toUnit) {
        if (fromUnit == toUnit || amount == 0) {
            return amount;
        }

        double convertedAmount = amount;

        // Conversion logic for volume units
        if (fromUnit == MILLILITRES) {
            if (toUnit == LITRES) {
                convertedAmount = amount / 1000; // 1 litre = 1000 millilitres
            } else if (toUnit == CUPS) {
                convertedAmount = amount / 236; // Rounded to 236 mL in a cup
            } else if (toUnit == TABLESPOONS) {
                convertedAmount = amount / 15; // Rounded to 15 mL in a tablespoon
            } else if (toUnit == TEASPOONS) {
                convertedAmount = amount / 5; // Rounded to 5 mL in a teaspoon
            }
        } else if (fromUnit == LITRES) {
            if (toUnit == MILLILITRES) {
                convertedAmount = amount * 1000; // 1 litre = 1000 millilitres
            } else if (toUnit == CUPS) {
                convertedAmount = amount * 4; // Approximately 4 cups in a litre
            } else if (toUnit == TABLESPOONS) {
                convertedAmount = amount * 68; // Approximately 68 tablespoons in a litre
            } else if (toUnit == TEASPOONS) {
                convertedAmount = amount * 202; // Approximately 202 teaspoons in a litre
            }
        } else if (fromUnit == CUPS) {
            if (toUnit == MILLILITRES) {
                convertedAmount = amount * 236; // Rounded to 236 mL in a cup
            } else if (toUnit == LITRES) {
                convertedAmount = amount / 4; // 1 cup = 1/4 litre
            } else if (toUnit == TABLESPOONS) {
                convertedAmount = amount * 16; // 1 cup = 16 tablespoons
            } else if (toUnit == TEASPOONS) {
                convertedAmount = amount * 48; // 1 cup = 48 teaspoons
            }
        } else if (fromUnit == TABLESPOONS) {
            if (toUnit == MILLILITRES) {
                convertedAmount = amount * 15; // Rounded to 15 mL in a tablespoon
            } else if (toUnit == LITRES) {
                convertedAmount = amount / 68; // Approximately 68 tablespoons in a litre
            } else if (toUnit == CUPS) {
                convertedAmount = amount / 16; // 1 tablespoon = 1/16 cups
            } else if (toUnit == TEASPOONS) {
                convertedAmount = amount * 3; // 1 tablespoon = 3 teaspoons
            }
        } else if (fromUnit == TEASPOONS) {
            if (toUnit == MILLILITRES) {
                convertedAmount = amount * 5; // Rounded to 5 mL in a teaspoon
            } else if (toUnit == LITRES) {
                convertedAmount = amount / 202; // Approximately 202 teaspoons in a litre
            } else if (toUnit == CUPS) {
                convertedAmount = amount / 48; // 1 teaspoon = 1/48 cups
            } else if (toUnit == TABLESPOONS) {
                convertedAmount = amount / 3; // 1 teaspoon = 1/3 tablespoons
            }
        }
        // Conversion logic for Mass units
        else if (fromUnit == GRAMS) {
            if (toUnit == KILOGRAMS) {
                convertedAmount = amount / 1000; // 1 kilogram = 1000 grams
            } else if (toUnit == OUNCES) {
                convertedAmount = amount / 28; // Approximately 28 grams in an ounce
            }
        } else if (fromUnit == KILOGRAMS) {
            if (toUnit == GRAMS) {
                convertedAmount = amount * 1000; // 1 kilogram = 1000 grams
            } else if (toUnit == OUNCES) {
                convertedAmount = amount * 35; // Approximately 35 ounces in a kilogram
            }
        } else if (fromUnit == OUNCES) {
            if (toUnit == GRAMS) {
                convertedAmount = amount * 28; // Approximately 28 grams in an ounce
            } else if (toUnit == KILOGRAMS) {
                convertedAmount = amount / 35; // Approximately 35 ounces in a kilogram
            }
        }

        if (convertedAmount == amount) {
            throw new IllegalArgumentException("Detected missing case / unit-conversion should not leave amount unchanged!");
        }

        return convertedAmount;
    }

    public boolean canConvert(IngredientUnit fromUnit, IngredientUnit toUnit) {
        if (fromUnit == toUnit) {
            return true;
        }

        if (fromUnit == PIECE || toUnit == PIECE) {
            return false; // Cannot convert PIECE
        }

        // Check if volumes
        boolean isVolumeUnit = (fromUnit == LITRES || fromUnit == MILLILITRES ||
                fromUnit == CUPS || fromUnit == TABLESPOONS ||
                fromUnit == TEASPOONS);

        boolean isTargetVolumeUnit = (toUnit == LITRES || toUnit == MILLILITRES ||
                toUnit == CUPS || toUnit == TABLESPOONS ||
                toUnit == TEASPOONS);

        if (isVolumeUnit && isTargetVolumeUnit) {
            return true;
        }

        // Check if weight
        boolean isWeightUnit = (fromUnit == KILOGRAMS || fromUnit == GRAMS || fromUnit == OUNCES);
        boolean isTargetWeightUnit = (toUnit == KILOGRAMS || toUnit == GRAMS || toUnit == OUNCES);

        return isWeightUnit && isTargetWeightUnit;
    }

    public IngredientUse convertIfPossible(IngredientUse ingredientUse, IngredientUnit toUnit) {
        if (canConvert(ingredientUse.unit(), toUnit)) {
            return new IngredientUse(ingredientUse.ingredient(), toUnit,
                    convertAmountTo(ingredientUse.amount(), ingredientUse.unit(), toUnit));
        } else {
            return ingredientUse;
        }
    }

    /**
     * Some rules to not show absurd units.
     */
    public IngredientUse heuristicallyConvertToNicerUnit(IngredientUse ingredientUse) {
        double amount = ingredientUse.amount();
        IngredientUnit unit = ingredientUse.unit();

        if (unit == GRAMS && amount >= 500) {
            return convertIfPossible(ingredientUse, KILOGRAMS);
        }
        if (unit == KILOGRAMS && amount < 0.5) {
            return convertIfPossible(ingredientUse, GRAMS);
        }
        if (unit == MILLILITRES && amount >= 500) {
            return convertIfPossible(ingredientUse, LITRES);
        }
        if (unit == LITRES && amount < 0.5) {
            return convertIfPossible(ingredientUse, MILLILITRES);
        }
        if (unit == TEASPOONS && amount >= 3) {
            return convertIfPossible(ingredientUse, TABLESPOONS);
        }
        if (unit == TABLESPOONS && amount < 1) {
            return convertIfPossible(ingredientUse, TEASPOONS);
        }
        if (unit == TABLESPOONS && amount >= 16) {
            return convertIfPossible(ingredientUse, CUPS);
        }
        if (unit == CUPS && amount < 1) {
            return convertIfPossible(ingredientUse, TABLESPOONS);
        }
        return ingredientUse;
    }

    public boolean ingredientAmountNotMinisculeHeuristic(IngredientUse ingredient) {
        double limit = switch (ingredient.unit()) {
            case LITRES -> 0.002;
            case MILLILITRES -> 1;
            case KILOGRAMS -> 0.001;
            case GRAMS -> 1;
            case OUNCES -> 0.01;
            case CUPS -> 0.1;
            case TABLESPOONS -> 0.25;
            case TEASPOONS -> 0.25;
            case PIECE -> 0.25;
        };
        return Math.abs(ingredient.amount()) >= limit;
    }

    public IngredientUse convertHeuristic(Map<Long, List<IngredientUnit>> map, IngredientUse ingredientUse) {
        IngredientUnit toUnit = getUnitHeuristic(map, ingredientUse);
        return convertIfPossible(ingredientUse, toUnit);
    }

    private IngredientUnit getUnitHeuristic(Map<Long, List<IngredientUnit>> map, IngredientUse ingredientUse) {
        return Stream.concat(map.computeIfAbsent(ingredientUse.ingredient().getId(), k -> Collections.emptyList()).stream(),
                        Stream.of(ingredientUse.ingredient().getDefaultUnit(), ingredientUse.unit()))
                .filter(otherUnit -> canConvert(ingredientUse.unit(), otherUnit))
                .findFirst().orElseThrow();
    }
}
