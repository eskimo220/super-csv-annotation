/*
 * ParseShort.java
 * created in 2013/03/06
 *
 * (C) Copyright 2003-2013 GreenDay Project. All rights reserved.
 */
package org.supercsv.ext.cellprocessor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;

import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.cellprocessor.ift.StringCellProcessor;
import org.supercsv.exception.SuperCsvCellProcessorException;
import org.supercsv.ext.cellprocessor.ift.ValidationCellProcessor;
import org.supercsv.util.CsvContext;


/**
 *
 * 
 * @author T.TSUCHIE
 *
 */
public class ParseLocaleNumber<N extends Number> extends CellProcessorAdaptor
        implements StringCellProcessor, ValidationCellProcessor {
    
    protected final Class<N> type;
    
    protected final String pattern;
    
    protected final boolean lenient;
    
    protected final Currency currency;
    
    protected final DecimalFormatSymbols symbols;
    
    protected final ThreadLocal<NumberFormat> formatter = new ThreadLocal<NumberFormat>(){
        
        @Override
        protected NumberFormat initialValue() {
            DecimalFormat value = null;
            if(symbols != null) {
                value = new DecimalFormat(pattern, symbols);
            } else {
                value = new DecimalFormat(pattern);
            }
            
            value.setParseBigDecimal(true);
            
            if(currency != null) {
                value.setCurrency(currency);
            }
            
            return value;
        }
    };
    
    public ParseLocaleNumber(final Class<N> type, final String pattern) {
        this(type, pattern, true, null, null);
        
    }
    
    public ParseLocaleNumber(final Class<N> type, final String pattern, final StringCellProcessor next) {
        this(type, pattern, true, null, null, next);
        
    }
    
    public ParseLocaleNumber(final Class<N> type, final String pattern, final boolean lenient,
            final Currency currency, final DecimalFormatSymbols symbols) {
        super();
        checkPreconditions(pattern);
        this.type = type;
        this.pattern = pattern;
        this.lenient = lenient;
        this.currency = currency;
        this.symbols = symbols;
        
    }
    
    public ParseLocaleNumber(final Class<N> type, final String pattern, final boolean lenient,
            final Currency currency, final DecimalFormatSymbols symbols, StringCellProcessor next) {
        super(next);
        checkPreconditions(pattern);
        this.type = type;
        this.pattern = pattern;
        this.lenient = lenient;
        this.currency = currency;
        this.symbols = symbols;        
    }
    
    /**
     * Checks the preconditions for creating a new ParseDate processor.
     * @throws IllegalArgumentException
     * 
     */
    protected static void checkPreconditions(final String pattern) {
        if(pattern == null || pattern.isEmpty() ) {
            throw new IllegalArgumentException("pattern should not be null");
        }
    }
    
    @Override
    public Object execute(final Object value, final CsvContext context) {
        validateInputNotNull(value, context);
        
        if(!(value instanceof String)) {
            throw new SuperCsvCellProcessorException(String.class, value, context, this);
        }
        
        try {
            Object result = parse((String) value);
            return next.execute(result, context);
            
        } catch(ParseException e) {
            throw new SuperCsvCellProcessorException(
                    String.format("'%s' could not be parsed as a BigDecimal", value),
                    context, this, e);
        }
    }
    
    protected Object parse(final String value) throws ParseException {
        
        final BigDecimal result = (BigDecimal) formatter.get().parse(value);
        
        if(type.isAssignableFrom(Byte.class) || type.isAssignableFrom(byte.class)) {
            return lenient ? result.byteValue() : result.byteValueExact();
        } else if(type.isAssignableFrom(Short.class) || type.isAssignableFrom(short.class)) {
            return lenient ? result.shortValue() : result.shortValueExact();
        } else if(type.isAssignableFrom(Integer.class) || type.isAssignableFrom(int.class)) {
            return lenient ? result.intValue() : result.intValueExact();
        } else if(type.isAssignableFrom(Long.class) || type.isAssignableFrom(long.class)) {
            return lenient ? result.longValue() : result.longValueExact();
        } else if(type.isAssignableFrom(Float.class) || type.isAssignableFrom(float.class)) {
            return result.floatValue();
        } else if(type.isAssignableFrom(Double.class) || type.isAssignableFrom(double.class)) {
            return result.doubleValue();
        } else if(type.isAssignableFrom(BigInteger.class)) {
            return lenient ? result.toBigInteger() : result.toBigIntegerExact();
        }
        
        return result;
    }
    
    
    public Class<N> getType() {
        return type;
    }
    
    public String getPattern() {
        return pattern;
    }
    
    public Currency getCurrency() {
        return currency;
    }
    
    public DecimalFormatSymbols getSymbols() {
        return symbols;
    }
    
    public boolean isLenient() {
        return lenient;
    }
    
    public ThreadLocal<NumberFormat> getFormatter() {
        return formatter;
    }

    @Override
    public String getMessageCode() {
        return ParseLocaleNumber.class.getCanonicalName() + ".violated";
    }

    @Override
    public Map<String, ?> getMessageVariable() {
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("pattern", getPattern());
        vars.put("currency", getCurrency());
        vars.put("symbols", getSymbols().getCurrencySymbol());
        vars.put("lenient", isLenient());
        
        return vars;
    }
    
    @Override
    public String formatValue(final Object value) {
        if(value == null) {
            return "";
        }
        return value.toString();
    }
    
}
