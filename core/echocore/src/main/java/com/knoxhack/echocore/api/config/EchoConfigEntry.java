package com.knoxhack.echocore.api.config;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class EchoConfigEntry {
    private final String id;
    private final String label;
    private final String description;
    private final EchoConfigSide side;
    private final EchoConfigValueKind kind;
    private final Supplier<String> valueSupplier;
    private final Supplier<String> defaultSupplier;
    private final Function<String, EchoConfigValidation> validator;
    private final Consumer<String> applier;
    private final Runnable resetter;
    private final String minValue;
    private final String maxValue;
    private final List<String> options;
    private final boolean editable;
    private final boolean restartRequired;
    private final boolean newWorldOnly;

    private EchoConfigEntry(
            String id,
            String label,
            String description,
            EchoConfigSide side,
            EchoConfigValueKind kind,
            Supplier<String> valueSupplier,
            Supplier<String> defaultSupplier,
            Function<String, EchoConfigValidation> validator,
            Consumer<String> applier,
            Runnable resetter,
            String minValue,
            String maxValue,
            List<String> options,
            boolean editable,
            boolean restartRequired,
            boolean newWorldOnly) {
        this.id = requireId(id, "config entry");
        this.label = clean(label, this.id);
        this.description = clean(description, "");
        this.side = side == null ? EchoConfigSide.COMMON : side;
        this.kind = kind == null ? EchoConfigValueKind.STRING : kind;
        this.valueSupplier = valueSupplier == null ? () -> "" : valueSupplier;
        this.defaultSupplier = defaultSupplier == null ? () -> "" : defaultSupplier;
        this.validator = validator == null ? EchoConfigValidation::ok : validator;
        this.applier = applier == null ? ignored -> { } : applier;
        this.resetter = resetter == null ? () -> this.applier.accept(this.defaultSupplier.get()) : resetter;
        this.minValue = clean(minValue, "");
        this.maxValue = clean(maxValue, "");
        this.options = List.copyOf(options == null
                ? List.of()
                : options.stream().filter(option -> option != null && !option.isBlank()).map(String::strip).toList());
        this.editable = editable;
        this.restartRequired = restartRequired;
        this.newWorldOnly = newWorldOnly;
    }

    public String id() {
        return id;
    }

    public EchoConfigSide side() {
        return side;
    }

    public EchoConfigEntrySnapshot snapshot(String moduleId, String categoryId) {
        String status = "";
        String value = "";
        try {
            value = clean(valueSupplier.get(), "");
        } catch (RuntimeException exception) {
            value = clean(defaultSupplier.get(), "");
            status = "Config value is not loaded yet.";
        }
        String defaultValue;
        try {
            defaultValue = clean(defaultSupplier.get(), "");
        } catch (RuntimeException exception) {
            defaultValue = "";
            if (status.isBlank()) {
                status = "Config default is not available.";
            }
        }
        return new EchoConfigEntrySnapshot(moduleId, categoryId, id, label, description, side, kind,
                value, defaultValue, minValue, maxValue, options, editable, restartRequired, newWorldOnly, status);
    }

    public EchoConfigApplyResult apply(String moduleId, String rawValue) {
        if (!editable) {
            return EchoConfigApplyResult.failure(moduleId, id, "This config entry is read-only.");
        }
        EchoConfigValidation validation = validator.apply(rawValue == null ? "" : rawValue.strip());
        if (!validation.valid()) {
            return EchoConfigApplyResult.failure(moduleId, id,
                    validation.message().isBlank() ? "Invalid config value." : validation.message());
        }
        try {
            applier.accept(validation.normalizedValue());
            return EchoConfigApplyResult.success(moduleId, id, valueSupplier.get(), "Config updated.");
        } catch (RuntimeException exception) {
            return EchoConfigApplyResult.failure(moduleId, id,
                    exception.getMessage() == null ? "Config update failed." : exception.getMessage());
        }
    }

    public EchoConfigApplyResult reset(String moduleId) {
        if (!editable) {
            return EchoConfigApplyResult.failure(moduleId, id, "This config entry is read-only.");
        }
        try {
            resetter.run();
            return EchoConfigApplyResult.success(moduleId, id, valueSupplier.get(), "Config reset.");
        } catch (RuntimeException exception) {
            return EchoConfigApplyResult.failure(moduleId, id,
                    exception.getMessage() == null ? "Config reset failed." : exception.getMessage());
        }
    }

    public static EchoConfigEntry booleanEntry(
            String id,
            String label,
            String description,
            EchoConfigSide side,
            boolean defaultValue,
            Supplier<Boolean> current,
            Consumer<Boolean> setter,
            Runnable saver,
            boolean editable,
            boolean restartRequired,
            boolean newWorldOnly) {
        Supplier<String> currentText = () -> String.valueOf(Boolean.TRUE.equals(current.get()));
        Supplier<String> defaultText = () -> String.valueOf(defaultValue);
        Consumer<String> apply = value -> {
            setter.accept(Boolean.parseBoolean(value));
            if (saver != null) {
                saver.run();
            }
        };
        return new EchoConfigEntry(id, label, description, side, EchoConfigValueKind.BOOLEAN,
                currentText, defaultText, EchoConfigEntry::validateBoolean, apply,
                () -> apply.accept(defaultText.get()), "", "", List.of("true", "false"),
                editable, restartRequired, newWorldOnly);
    }

    public static EchoConfigEntry intEntry(
            String id,
            String label,
            String description,
            EchoConfigSide side,
            int defaultValue,
            int min,
            int max,
            Supplier<Integer> current,
            Consumer<Integer> setter,
            Runnable saver,
            boolean editable,
            boolean restartRequired,
            boolean newWorldOnly) {
        Supplier<String> currentText = () -> String.valueOf(current.get());
        Supplier<String> defaultText = () -> String.valueOf(defaultValue);
        Consumer<String> apply = value -> {
            setter.accept(Integer.parseInt(value));
            if (saver != null) {
                saver.run();
            }
        };
        return new EchoConfigEntry(id, label, description, side, EchoConfigValueKind.INTEGER,
                currentText, defaultText, value -> validateInt(value, min, max), apply,
                () -> apply.accept(defaultText.get()), String.valueOf(min), String.valueOf(max), List.of(),
                editable, restartRequired, newWorldOnly);
    }

    public static EchoConfigEntry doubleEntry(
            String id,
            String label,
            String description,
            EchoConfigSide side,
            double defaultValue,
            double min,
            double max,
            Supplier<Double> current,
            Consumer<Double> setter,
            Runnable saver,
            boolean editable,
            boolean restartRequired,
            boolean newWorldOnly) {
        Supplier<String> currentText = () -> String.valueOf(current.get());
        Supplier<String> defaultText = () -> String.valueOf(defaultValue);
        Consumer<String> apply = value -> {
            setter.accept(Double.parseDouble(value));
            if (saver != null) {
                saver.run();
            }
        };
        return new EchoConfigEntry(id, label, description, side, EchoConfigValueKind.DOUBLE,
                currentText, defaultText, value -> validateDouble(value, min, max), apply,
                () -> apply.accept(defaultText.get()), String.valueOf(min), String.valueOf(max), List.of(),
                editable, restartRequired, newWorldOnly);
    }

    public static <T extends Enum<T>> EchoConfigEntry enumEntry(
            String id,
            String label,
            String description,
            EchoConfigSide side,
            T defaultValue,
            Class<T> enumClass,
            Supplier<T> current,
            Consumer<T> setter,
            Runnable saver,
            boolean editable,
            boolean restartRequired,
            boolean newWorldOnly) {
        List<String> options = Arrays.stream(enumClass.getEnumConstants()).map(Enum::name).toList();
        Supplier<String> currentText = () -> current.get().name();
        Supplier<String> defaultText = () -> defaultValue.name();
        Consumer<String> apply = value -> {
            setter.accept(Enum.valueOf(enumClass, value));
            if (saver != null) {
                saver.run();
            }
        };
        return new EchoConfigEntry(id, label, description, side, EchoConfigValueKind.ENUM,
                currentText, defaultText, value -> validateEnum(value, options), apply,
                () -> apply.accept(defaultText.get()), "", "", options, editable, restartRequired, newWorldOnly);
    }

    public static EchoConfigEntry stringEntry(
            String id,
            String label,
            String description,
            EchoConfigSide side,
            String defaultValue,
            Supplier<String> current,
            Consumer<String> setter,
            Runnable saver,
            boolean editable,
            boolean restartRequired,
            boolean newWorldOnly) {
        Supplier<String> currentText = () -> current.get() == null ? "" : current.get();
        Supplier<String> defaultText = () -> defaultValue == null ? "" : defaultValue;
        Consumer<String> apply = value -> {
            setter.accept(value);
            if (saver != null) {
                saver.run();
            }
        };
        return new EchoConfigEntry(id, label, description, side, EchoConfigValueKind.STRING,
                currentText, defaultText, EchoConfigValidation::ok, apply,
                () -> apply.accept(defaultText.get()), "", "", List.of(), editable, restartRequired, newWorldOnly);
    }

    public static EchoConfigEntry booleanSpec(
            String id,
            String label,
            String description,
            EchoConfigSide side,
            ModConfigSpec.BooleanValue value,
            boolean editable,
            boolean restartRequired,
            boolean newWorldOnly) {
        return booleanEntry(id, label, specDescription(description, value), side, value.getDefault(),
                value::get, value::set, value::save, editable, restartRequired, newWorldOnly);
    }

    public static EchoConfigEntry intSpec(
            String id,
            String label,
            String description,
            EchoConfigSide side,
            ModConfigSpec.IntValue value,
            int min,
            int max,
            boolean editable,
            boolean restartRequired,
            boolean newWorldOnly) {
        return intEntry(id, label, specDescription(description, value), side, value.getDefault(), min, max,
                value::get, value::set, value::save, editable, restartRequired, newWorldOnly);
    }

    public static EchoConfigEntry doubleSpec(
            String id,
            String label,
            String description,
            EchoConfigSide side,
            ModConfigSpec.DoubleValue value,
            double min,
            double max,
            boolean editable,
            boolean restartRequired,
            boolean newWorldOnly) {
        return doubleEntry(id, label, specDescription(description, value), side, value.getDefault(), min, max,
                value::get, value::set, value::save, editable, restartRequired, newWorldOnly);
    }

    public static <T extends Enum<T>> EchoConfigEntry enumSpec(
            String id,
            String label,
            String description,
            EchoConfigSide side,
            ModConfigSpec.EnumValue<T> value,
            Class<T> enumClass,
            boolean editable,
            boolean restartRequired,
            boolean newWorldOnly) {
        return enumEntry(id, label, specDescription(description, value), side, value.getDefault(), enumClass,
                value::get, value::set, value::save, editable, restartRequired, newWorldOnly);
    }

    public static EchoConfigEntry stringSpec(
            String id,
            String label,
            String description,
            EchoConfigSide side,
            ModConfigSpec.ConfigValue<String> value,
            boolean editable,
            boolean restartRequired,
            boolean newWorldOnly) {
        return stringEntry(id, label, specDescription(description, value), side, value.getDefault(),
                value::get, value::set, value::save, editable, restartRequired, newWorldOnly);
    }

    private static EchoConfigValidation validateBoolean(String value) {
        String normalized = clean(value, "").toLowerCase(Locale.ROOT);
        if ("true".equals(normalized) || "1".equals(normalized) || "yes".equals(normalized) || "on".equals(normalized)) {
            return EchoConfigValidation.ok("true");
        }
        if ("false".equals(normalized) || "0".equals(normalized) || "no".equals(normalized) || "off".equals(normalized)) {
            return EchoConfigValidation.ok("false");
        }
        return EchoConfigValidation.error("Expected true or false.");
    }

    private static EchoConfigValidation validateInt(String value, int min, int max) {
        try {
            int parsed = Integer.parseInt(clean(value, ""));
            if (parsed < min || parsed > max) {
                return EchoConfigValidation.error("Expected a value from " + min + " to " + max + ".");
            }
            return EchoConfigValidation.ok(String.valueOf(parsed));
        } catch (NumberFormatException exception) {
            return EchoConfigValidation.error("Expected a whole number.");
        }
    }

    private static EchoConfigValidation validateDouble(String value, double min, double max) {
        try {
            double parsed = Double.parseDouble(clean(value, ""));
            if (!Double.isFinite(parsed) || parsed < min || parsed > max) {
                return EchoConfigValidation.error("Expected a value from " + min + " to " + max + ".");
            }
            return EchoConfigValidation.ok(String.valueOf(parsed));
        } catch (NumberFormatException exception) {
            return EchoConfigValidation.error("Expected a number.");
        }
    }

    private static EchoConfigValidation validateEnum(String value, List<String> options) {
        String normalized = clean(value, "").replace(' ', '_').replace('-', '_');
        for (String option : options) {
            if (option.equalsIgnoreCase(normalized)) {
                return EchoConfigValidation.ok(option);
            }
        }
        return EchoConfigValidation.error("Expected one of: " + String.join(", ", options) + ".");
    }

    private static String specDescription(String description, ModConfigSpec.ConfigValue<?> value) {
        String cleaned = clean(description, "");
        if (!cleaned.isBlank()) {
            return cleaned;
        }
        try {
            String comment = value.getSpec().getComment();
            return clean(comment, "");
        } catch (RuntimeException exception) {
            return "";
        }
    }

    private static String clean(String value, String fallback) {
        String cleaned = value == null ? "" : value.strip();
        return cleaned.isBlank() ? fallback : cleaned;
    }

    static String requireId(String value, String label) {
        String raw = value == null ? "" : value.strip();
        String id = raw.toLowerCase(Locale.ROOT);
        if (id.isBlank()) {
            throw new IllegalArgumentException(label + " id is required.");
        }
        if (!id.equals(raw)) {
            throw new IllegalArgumentException(label + " id must be lowercase: " + value);
        }
        for (int i = 0; i < id.length(); i++) {
            char c = id.charAt(i);
            boolean allowed = c >= 'a' && c <= 'z'
                    || c >= '0' && c <= '9'
                    || c == '_' || c == '-' || c == '.';
            if (!allowed) {
                throw new IllegalArgumentException(label + " id has unsupported character: " + value);
            }
        }
        return id;
    }
}
