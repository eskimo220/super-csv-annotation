package org.supercsv.ext.builder.impl;

import java.lang.annotation.Annotation;
import java.text.NumberFormat;
import java.text.ParseException;

import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.cellprocessor.ift.DoubleCellProcessor;
import org.supercsv.cellprocessor.ift.StringCellProcessor;
import org.supercsv.ext.annotation.CsvColumn;
import org.supercsv.ext.annotation.CsvNumberConverter;
import org.supercsv.ext.cellprocessor.FormatLocaleNumber;
import org.supercsv.ext.cellprocessor.ParseFloat;
import org.supercsv.ext.cellprocessor.ParseLocaleNumber;
import org.supercsv.ext.exception.SuperCsvInvalidAnnotationException;

public class FloatCellProcessorBuilder extends AbstractNumberCellProcessorBuilder<Float> {
    
    @Override
    protected CellProcessor buildInputCellProcessorWithConvertNullTo(final Class<Float> type, final Annotation[] annos,
            final CellProcessor cellProcessor, final CsvColumn csvColumnAnno) {
        
        // プリミティブ型の場合、オプションかつ初期値が与えられていない場合、0に変換する。
        if(type.isPrimitive() && csvColumnAnno.optional() && csvColumnAnno.inputDefaultValue().isEmpty()) {
            return prependConvertNullToProcessor(type, cellProcessor, 0.0f);
            
        } else if(!csvColumnAnno.inputDefaultValue().isEmpty()) {
            return prependConvertNullToProcessor(type, cellProcessor,
                    getParseValue(type, annos, csvColumnAnno.inputDefaultValue()));
        }
        
        return cellProcessor;
    }
    
    @Override
    public CellProcessor buildOutputCellProcessor(final Class<Float> type, final Annotation[] annos,
            final CellProcessor processor, final boolean ignoreValidationProcessor) {
        
        final CsvNumberConverter converterAnno = getAnnotation(annos);
        final NumberFormat formatter = createNumberFormatter(converterAnno);
        
        final Float min = getParseValue(type, annos, getMin(converterAnno));
        final Float max = getParseValue(type, annos, getMax(converterAnno));
        
        CellProcessor cp = processor;
        if(formatter != null) {
            cp = (cp == null ?
                    new FormatLocaleNumber(formatter) : new FormatLocaleNumber(formatter, (StringCellProcessor) cp));
        }
        
        if(!ignoreValidationProcessor) {
            cp = prependRangeProcessor(min, max, formatter, cp);
        }
        
        return cp;
    }
    
    @Override
    public CellProcessor buildInputCellProcessor(final Class<Float> type, final Annotation[] annos,
            final CellProcessor processor) {
        
        final CsvNumberConverter converterAnno = getAnnotation(annos);
        final NumberFormat formatter = createNumberFormatter(converterAnno);
        final boolean lenient = getLenient(converterAnno);
        
        final Float min = getParseValue(type, annos, getMin(converterAnno));
        final Float max = getParseValue(type, annos, getMax(converterAnno));
        
        CellProcessor cp = processor;
        cp = prependRangeProcessor(min, max, formatter, cp);
        
        if(formatter != null) {
            cp = (cp == null ?
                    new ParseLocaleNumber<Float>(type, formatter, lenient) :
                        new ParseLocaleNumber<Float>(type, formatter, lenient, cp));
                
        } else {
            cp = (cp == null ?
                    new ParseFloat() : new ParseFloat((DoubleCellProcessor) cp));
        }
        
        return cp;
        
    }
    
    @Override
    public Float getParseValue(final Class<Float> type, final Annotation[] annos, final String strValue) {
        
        if(strValue.isEmpty()) {
            return null;
        }
        
        final CsvNumberConverter converterAnno = getAnnotation(annos);
        final NumberFormat formatter = createNumberFormatter(converterAnno);
        final String pattern = getPattern(converterAnno);
        
        if(formatter != null) {
            try {
                return formatter.parse(strValue).floatValue();
            } catch(ParseException e) {
                throw new SuperCsvInvalidAnnotationException(
                        String.format(" value '%s' cannot parse to Number with pattern '%s'", strValue, pattern),
                        e);
            }
        } else {
            return Float.valueOf(strValue);
        }
    }
    
}
