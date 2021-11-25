package nl.tudelft.sem.template.converters;

import java.util.Arrays;
import java.util.List;
import javax.persistence.AttributeConverter;

/**
 * Database converter for attributes of type List of String.
 */
public class StringListConverter implements AttributeConverter<List<String>, String> {

    /**
     * Converts a List of String to a string separating the elements by semicolons.
     *
     * @param attribute List to convert.
     * @return String containing the attribute elements seperated by semicolons.
     */
    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        if (attribute == null) {
            return "";
        }
        String res = "";
        for (int i = 0; i < attribute.size(); i++) {
            res += attribute.get(i);
            if (i < attribute.size() - 1) {
                res += ";";
            }
        }
        return res;
    }

    /**
     * Converts a String to a list of String using semicolon as delimiter.
     *
     * @param dbData String to convert.
     * @return List of String containing the elements from the input.
     */
    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        return Arrays.asList(dbData.split(";"));
    }
}
