package com.github.mygreen.supercsv.builder.time;

import static org.junit.Assert.*;
import static org.assertj.core.api.Assertions.*;
import static com.github.mygreen.supercsv.tool.TestUtils.*;
import static com.github.mygreen.supercsv.tool.HasCellProcessorAssert.*;

import java.time.ZoneOffset;
import java.time.OffsetTime;
import java.util.List;
import java.util.ResourceBundle;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.supercsv.cellprocessor.ift.CellProcessor;

import com.github.mygreen.supercsv.annotation.CsvBean;
import com.github.mygreen.supercsv.annotation.CsvColumn;
import com.github.mygreen.supercsv.annotation.format.CsvDateTimeFormat;
import com.github.mygreen.supercsv.builder.BeanMapping;
import com.github.mygreen.supercsv.builder.BeanMappingFactory;
import com.github.mygreen.supercsv.builder.ColumnMapping;
import com.github.mygreen.supercsv.exception.SuperCsvValidationException;
import com.github.mygreen.supercsv.localization.MessageResolver;
import com.github.mygreen.supercsv.localization.ResourceBundleMessageResolver;
import com.github.mygreen.supercsv.validation.CsvExceptionConverter;

/**
 * {@link OffsetTimeProcessorBuilder}のテスタ
 *
 * @since 2.1
 * @author T.TSUCHIE
 *
 */
public class OffsetTimeProcessorBuilderTest {
    
    @Rule
    public TestName name = new TestName();
    
    private BeanMappingFactory beanMappingFactory;
    private CsvExceptionConverter exceptionConverter;
    
    private final Class<?>[] groupEmpty = new Class[]{};
    
    private MessageResolver testMessageResolver;
    
    @Before
    public void setUp() {
        this.beanMappingFactory = new BeanMappingFactory();
        this.exceptionConverter = new CsvExceptionConverter();
        
        this.testMessageResolver = new ResourceBundleMessageResolver(ResourceBundle.getBundle("TestMessages"));
    }
    
    private static final String TEST_DEFAULT_PATTERN = "HH:mm:ssxxx";
    private static final String TEST_FORMATTED_PATTERN = "H:m:sxxx";
    
    private static final OffsetTime TEST_VALUE_1_OBJ = OffsetTime.of(7, 12, 1, 0, ZoneOffset.ofHours(9));
    private static final String TEST_VALUE_1_STR_NORMAL = "07:12:01+09:00";
    private static final String TEST_VALUE_1_STR_FORMATTED = "7:12:1+09:00";
    
    private static final String TEST_VALUE_1_STR_LENIENT = "07:12:01+09:00";
    private static final OffsetTime TEST_VALUE_1_OBJ_TIMEZONE = OffsetTime.of(16, 12, 1, 0, ZoneOffset.ofHours(9));
//    private static final String TEST_VALUE_1_STR_TIMEZONE = "22:12:01+00:00";
    private static final String TEST_VALUE_1_STR_TIMEZONE = "07:12:01+09:00";
    
    @CsvBean
    private static class TestCsv {
        
        @CsvColumn(number=1)
        OffsetTime col_default;
        
        @CsvColumn(number=2)
        @CsvDateTimeFormat(pattern=TEST_FORMATTED_PATTERN)
        OffsetTime col_format;
        
        @CsvColumn(number=3)
        @CsvDateTimeFormat(lenient=true)
        OffsetTime col_lenient;
        
        @CsvColumn(number=4)
        @CsvDateTimeFormat(timezone="GMT+9")
        OffsetTime col_timezone;
        
        @CsvColumn(number=10)
        @CsvDateTimeFormat(message="テストメッセージ")
        OffsetTime col_message;
        
        @CsvColumn(number=11)
        @CsvDateTimeFormat(message="")
        OffsetTime col_message_empty;
        
        @CsvColumn(number=12)
        @CsvDateTimeFormat(pattern=TEST_FORMATTED_PATTERN,
                message="lineNumber={lineNumber}, rowNumber={rowNumber}, columnNumber={columnNumber}, label={label}, validatedValue={validatedValue}, pattern={pattern}")
        OffsetTime col_message_variables;
        
    }
    
    @Test
    public void testBuildForReading_default() {
        
        BeanMapping<TestCsv> beanMapping = beanMappingFactory.create(TestCsv.class, groupEmpty);
        ColumnMapping columnMapping = beanMapping.getColumnMapping("col_default").get();
        
        CellProcessor processor = columnMapping.getCellProcessorForReading();
        printCellProcessorChain(processor, name.getMethodName());
        
        {
            // null input
            String input = null;
            OffsetTime expected = null;
            assertThat((Object)processor.execute(input, ANONYMOUS_CSVCONTEXT)).isEqualTo(expected);
        }
        
        {
            // empty input
            String input = "";
            OffsetTime expected = null;
            assertThat((Object)processor.execute(input, ANONYMOUS_CSVCONTEXT)).isEqualTo(expected);
        }
        
        {
            // valid input
            String input = TEST_VALUE_1_STR_NORMAL;
            OffsetTime expected = TEST_VALUE_1_OBJ;
            assertThat((Object)processor.execute(input, ANONYMOUS_CSVCONTEXT)).isEqualTo(expected);
        }
        
        {
            // wrong input
            String input = "abc";
            try {
                processor.execute(input, ANONYMOUS_CSVCONTEXT);
                fail();
                
            } catch(Exception e) {
                assertThat(e).isInstanceOf(SuperCsvValidationException.class);
                
                SuperCsvValidationException validationException = (SuperCsvValidationException)e;
                assertThat(validationException.isParedError()).isTrue();
                assertThat(validationException.getRejectedValue()).isEqualTo(input);
                assertThat(validationException.getValidationMessage()).isEqualTo("{com.github.mygreen.supercsv.cellprocessor.format.ParseProcessor.violated}");
                assertThat(validationException.getMessageVariables()).containsEntry("pattern", TEST_DEFAULT_PATTERN);
                
            }
        }
    }
    
    @Test
    public void testBuildForWriging_default() {
        
        BeanMapping<TestCsv> beanMapping = beanMappingFactory.create(TestCsv.class, groupEmpty);
        ColumnMapping columnMapping = beanMapping.getColumnMapping("col_default").get();
        
        CellProcessor processor = columnMapping.getCellProcessorForWriting();
        printCellProcessorChain(processor, name.getMethodName());
        
        {
            // null input
            OffsetTime input = null;
            String expected = null;
            assertThat((Object)processor.execute(input, ANONYMOUS_CSVCONTEXT)).isEqualTo(expected);
        }
        
        {
            // valid input
            OffsetTime input = TEST_VALUE_1_OBJ;
            String expected = TEST_VALUE_1_STR_NORMAL;
            assertThat((Object)processor.execute(input, ANONYMOUS_CSVCONTEXT)).isEqualTo(expected);
        }
        
    }
    
    @Test
    public void testBuildForReading_format() {
        
        BeanMapping<TestCsv> beanMapping = beanMappingFactory.create(TestCsv.class, groupEmpty);
        ColumnMapping columnMapping = beanMapping.getColumnMapping("col_format").get();
        
        CellProcessor processor = columnMapping.getCellProcessorForReading();
        printCellProcessorChain(processor, name.getMethodName());
        
        {
            // null input
            String input = null;
            String expected = null;
            assertThat((Object)processor.execute(input, ANONYMOUS_CSVCONTEXT)).isEqualTo(expected);
        }
        
        {
            // empty input
            String input = "";
            OffsetTime expected = null;
            assertThat((Object)processor.execute(input, ANONYMOUS_CSVCONTEXT)).isEqualTo(expected);
        }
        
        {
            // valid input
            String input = TEST_VALUE_1_STR_FORMATTED;
            OffsetTime expected = TEST_VALUE_1_OBJ;
            assertThat((Object)processor.execute(input, ANONYMOUS_CSVCONTEXT)).isEqualTo(expected);
        }
        
        {
            // wrong input
            String input = "abc";
            try {
                processor.execute(input, ANONYMOUS_CSVCONTEXT);
                fail();
                
            } catch(Exception e) {
                assertThat(e).isInstanceOf(SuperCsvValidationException.class);
                
                SuperCsvValidationException validationException = (SuperCsvValidationException)e;
                assertThat(validationException.isParedError()).isTrue();
                assertThat(validationException.getRejectedValue()).isEqualTo(input);
                assertThat(validationException.getValidationMessage()).isEqualTo("{com.github.mygreen.supercsv.annotation.format.CsvDateTimeFormat.message}");
                assertThat(validationException.getMessageVariables()).containsEntry("pattern", TEST_FORMATTED_PATTERN);
                
            }
        }
    }
    
    @Test
    public void testBuildForWriting_format() {
        
        BeanMapping<TestCsv> beanMapping = beanMappingFactory.create(TestCsv.class, groupEmpty);
        ColumnMapping columnMapping = beanMapping.getColumnMapping("col_format").get();
        
        CellProcessor processor = columnMapping.getCellProcessorForWriting();
        printCellProcessorChain(processor, name.getMethodName());
        
        {
            // null input
            OffsetTime input = null;
            String expected = null;
            assertThat((Object)processor.execute(input, ANONYMOUS_CSVCONTEXT)).isEqualTo(expected);
        }
        
        {
            // valid input
            OffsetTime input = TEST_VALUE_1_OBJ;
            String expected = TEST_VALUE_1_STR_FORMATTED;
            
            String actual = (String)processor.execute(input, ANONYMOUS_CSVCONTEXT);
            if(isLinuxPlatform()) {
                assertThat(actual.replace("[Japan]", "[Asia/Tokyo]")).isEqualTo(expected);
            } else {
                assertThat(actual).isEqualTo(expected);
            }
        }
        
    }
    
    @Test
    public void testBuildForReading_leinent() {
        
        BeanMapping<TestCsv> beanMapping = beanMappingFactory.create(TestCsv.class, groupEmpty);
        ColumnMapping columnMapping = beanMapping.getColumnMapping("col_lenient").get();
        
        CellProcessor processor = columnMapping.getCellProcessorForReading();
        printCellProcessorChain(processor, name.getMethodName());
        
        {
            // null input
            String input = null;
            String expected = null;
            assertThat((Object)processor.execute(input, ANONYMOUS_CSVCONTEXT)).isEqualTo(expected);
        }
        
        {
            // empty input
            String input = "";
            OffsetTime expected = null;
            assertThat((Object)processor.execute(input, ANONYMOUS_CSVCONTEXT)).isEqualTo(expected);
        }
        
        {
            // valid input
            String input = TEST_VALUE_1_STR_NORMAL;
            OffsetTime expected = TEST_VALUE_1_OBJ;
            assertThat((Object)processor.execute(input, ANONYMOUS_CSVCONTEXT)).isEqualTo(expected);
        }
        
        {
            // valid input - lenient
            String input = TEST_VALUE_1_STR_LENIENT;
            OffsetTime expected = TEST_VALUE_1_OBJ;
            assertThat((Object)processor.execute(input, ANONYMOUS_CSVCONTEXT)).isEqualTo(expected);
        }
        
        {
            // wrong input
            String input = "abc";
            try {
                processor.execute(input, ANONYMOUS_CSVCONTEXT);
                fail();
                
            } catch(Exception e) {
                assertThat(e).isInstanceOf(SuperCsvValidationException.class);
                
                SuperCsvValidationException validationException = (SuperCsvValidationException)e;
                assertThat(validationException.isParedError()).isTrue();
                assertThat(validationException.getRejectedValue()).isEqualTo(input);
                assertThat(validationException.getValidationMessage()).isEqualTo("{com.github.mygreen.supercsv.annotation.format.CsvDateTimeFormat.message}");
                assertThat(validationException.getMessageVariables()).containsEntry("pattern", TEST_DEFAULT_PATTERN);
                
            }
        }
    }
    
    @Test
    public void testBuildForWriting_lenient() {
        
        BeanMapping<TestCsv> beanMapping = beanMappingFactory.create(TestCsv.class, groupEmpty);
        ColumnMapping columnMapping = beanMapping.getColumnMapping("col_lenient").get();
        
        CellProcessor processor = columnMapping.getCellProcessorForWriting();
        printCellProcessorChain(processor, name.getMethodName());
        
        {
            // null input
            OffsetTime input = null;
            String expected = null;
            assertThat((Object)processor.execute(input, ANONYMOUS_CSVCONTEXT)).isEqualTo(expected);
        }
        
        {
            // valid input
            OffsetTime input = TEST_VALUE_1_OBJ;
            String expected = TEST_VALUE_1_STR_NORMAL;
            
            String actual = (String)processor.execute(input, ANONYMOUS_CSVCONTEXT);
            
            if(isLinuxPlatform()) {
                assertThat(actual.replace("[Japan]", "[Asia/Tokyo]")).isEqualTo(expected);
            } else {
                assertThat(actual).isEqualTo(expected);
            }
            
        }
        
    }
    
    @Test
    public void testBuildForReading_timezone() {
        
        BeanMapping<TestCsv> beanMapping = beanMappingFactory.create(TestCsv.class, groupEmpty);
        ColumnMapping columnMapping = beanMapping.getColumnMapping("col_timezone").get();
        
        CellProcessor processor = columnMapping.getCellProcessorForReading();
        printCellProcessorChain(processor, name.getMethodName());
        
        {
            // null input
            String input = null;
            String expected = null;
            assertThat((Object)processor.execute(input, ANONYMOUS_CSVCONTEXT)).isEqualTo(expected);
        }
        
        {
            // empty input
            String input = "";
            OffsetTime expected = null;
            assertThat((Object)processor.execute(input, ANONYMOUS_CSVCONTEXT)).isEqualTo(expected);
        }
        
        {
            // valid input - timezone
            String input = TEST_VALUE_1_STR_NORMAL;
            OffsetTime expected = TEST_VALUE_1_OBJ;
            assertThat((Object)processor.execute(input, ANONYMOUS_CSVCONTEXT)).isEqualTo(expected);
        }
        
        {
            // wrong input
            String input = "abc";
            try {
                processor.execute(input, ANONYMOUS_CSVCONTEXT);
                fail();
                
            } catch(Exception e) {
                assertThat(e).isInstanceOf(SuperCsvValidationException.class);
                
                SuperCsvValidationException validationException = (SuperCsvValidationException)e;
                assertThat(validationException.isParedError()).isTrue();
                assertThat(validationException.getRejectedValue()).isEqualTo(input);
                assertThat(validationException.getValidationMessage()).isEqualTo("{com.github.mygreen.supercsv.annotation.format.CsvDateTimeFormat.message}");
                assertThat(validationException.getMessageVariables()).containsEntry("pattern", TEST_DEFAULT_PATTERN);
                
            }
        }
    }
    
    @Test
    public void testBuildForWriting_timezone() {
        
        BeanMapping<TestCsv> beanMapping = beanMappingFactory.create(TestCsv.class, groupEmpty);
        ColumnMapping columnMapping = beanMapping.getColumnMapping("col_timezone").get();
        
        CellProcessor processor = columnMapping.getCellProcessorForWriting();
        printCellProcessorChain(processor, name.getMethodName());
        
        {
            // null input
            OffsetTime input = null;
            String expected = null;
            assertThat((Object)processor.execute(input, ANONYMOUS_CSVCONTEXT)).isEqualTo(expected);
        }
        
        {
            // valid input
            OffsetTime input = TEST_VALUE_1_OBJ;
            String expected = TEST_VALUE_1_STR_TIMEZONE;
            assertThat((Object)processor.execute(input, ANONYMOUS_CSVCONTEXT)).isEqualTo(expected);
        }
        
    }
    
    @Test
    public void testErrorMessage_default() {
        
        BeanMapping<TestCsv> beanMapping = beanMappingFactory.create(TestCsv.class, groupEmpty);
        ColumnMapping columnMapping = beanMapping.getColumnMapping("col_default").get();
        
        CellProcessor processor = columnMapping.getCellProcessorForReading();
        printCellProcessorChain(processor, name.getMethodName());
        
        String input = "abc";
        try {
            processor.execute(input, testCsvContext(columnMapping, input));
            fail();
            
        } catch(Exception e) {
            assertThat(e).isInstanceOf(SuperCsvValidationException.class);
            
            List<String> messages = exceptionConverter.convertAndFormat((SuperCsvValidationException)e, beanMapping);
            assertThat(messages).hasSize(1)
                    .contains("[2行, 1列] : 項目「col_default」の値（abc）の書式は不正です。");                
        }
        
    }
    
    /**
     * アノテーションの属性formatを指定した場合
     */
    @Test
    public void testErrorMessage_format() {
        
        BeanMapping<TestCsv> beanMapping = beanMappingFactory.create(TestCsv.class, groupEmpty);
        ColumnMapping columnMapping = beanMapping.getColumnMapping("col_format").get();
        
        CellProcessor processor = columnMapping.getCellProcessorForReading();
        printCellProcessorChain(processor, name.getMethodName());
        
        String input = "abc";
        try {
            processor.execute(input, testCsvContext(columnMapping, input));
            fail();
            
        } catch(Exception e) {
            assertThat(e).isInstanceOf(SuperCsvValidationException.class);
            
            List<String> messages = exceptionConverter.convertAndFormat((SuperCsvValidationException)e, beanMapping);
            assertThat(messages).hasSize(1)
                    .contains("[2行, 2列] : 項目「col_format」の値（abc）は、日時の書式「H:m:sxxx」として不正です。");                
        }
        
    }
    
    /**
     * アノテーションの属性messageの指定
     */
    @Test
    public void testErrorMessage_message() {
        
        BeanMapping<TestCsv> beanMapping = beanMappingFactory.create(TestCsv.class, groupEmpty);
        ColumnMapping columnMapping = beanMapping.getColumnMapping("col_message").get();
        
        CellProcessor processor = columnMapping.getCellProcessorForReading();
        printCellProcessorChain(processor, name.getMethodName());
        
        String input = "abc";
        try {
            processor.execute(input, testCsvContext(columnMapping, input));
            fail();
            
        } catch(Exception e) {
            assertThat(e).isInstanceOf(SuperCsvValidationException.class);
            
            List<String> messages = exceptionConverter.convertAndFormat((SuperCsvValidationException)e, beanMapping);
            assertThat(messages).hasSize(1)
                    .contains("テストメッセージ");                
        }
        
    }
    
    /**
     * アノテーションの属性messageの値が空文字の場合
     */
    @Test
    public void testErrorMessage_empty() {
        
        BeanMapping<TestCsv> beanMapping = beanMappingFactory.create(TestCsv.class, groupEmpty);
        ColumnMapping columnMapping = beanMapping.getColumnMapping("col_message_empty").get();
        
        CellProcessor processor = columnMapping.getCellProcessorForReading();
        printCellProcessorChain(processor, name.getMethodName());
        
        String input = "abc";
        try {
            processor.execute(input, testCsvContext(columnMapping, input));
            fail();
            
        } catch(Exception e) {
            assertThat(e).isInstanceOf(SuperCsvValidationException.class);
            
            SuperCsvValidationException validationException = (SuperCsvValidationException)e;
            assertThat(validationException.getValidationMessage()).isEqualTo("{com.github.mygreen.supercsv.cellprocessor.format.ParseProcessor.violated}");
            
            List<String> messages = exceptionConverter.convertAndFormat((SuperCsvValidationException)e, beanMapping);
            assertThat(messages).hasSize(1)
                    .contains("[2行, 11列] : 項目「col_message_empty」の値（abc）の書式は不正です。");                
        }
        
    }
    
    /**
     * アノテーションの属性messageの値が空文字の場合
     */
    @Test
    public void testErrorMessage_variables() {
        
        BeanMapping<TestCsv> beanMapping = beanMappingFactory.create(TestCsv.class, groupEmpty);
        ColumnMapping columnMapping = beanMapping.getColumnMapping("col_message_variables").get();
        
        CellProcessor processor = columnMapping.getCellProcessorForReading();
        printCellProcessorChain(processor, name.getMethodName());
        
        String input = "abc";
        try {
            processor.execute(input, testCsvContext(columnMapping, input));
            fail();
            
        } catch(Exception e) {
            assertThat(e).isInstanceOf(SuperCsvValidationException.class);
            
            List<String> messages = exceptionConverter.convertAndFormat((SuperCsvValidationException)e, beanMapping);
            assertThat(messages).hasSize(1)
                    .contains("lineNumber=1, rowNumber=2, columnNumber=12, label=col_message_variables, validatedValue=abc, pattern=H:m:sxxx");                
        }
        
    }
    
    /**
     * プロパティファイルのtypeMismatchからメッセージを取得する場合
     */
    @Test
    public void testErrorMessage_typeMismatch() {
        
        // 独自のメッセージに入れ替え
        exceptionConverter.setMessageResolver(testMessageResolver);
        
        BeanMapping<TestCsv> beanMapping = beanMappingFactory.create(TestCsv.class, groupEmpty);
        ColumnMapping columnMapping = beanMapping.getColumnMapping("col_default").get();
        
        CellProcessor processor = columnMapping.getCellProcessorForReading();
        printCellProcessorChain(processor, name.getMethodName());
        
        String input = "abc";
        try {
            processor.execute(input, testCsvContext(columnMapping, input));
            fail();
            
        } catch(Exception e) {
            assertThat(e).isInstanceOf(SuperCsvValidationException.class);
            
            List<String> messages = exceptionConverter.convertAndFormat((SuperCsvValidationException)e, beanMapping);
            assertThat(messages).hasSize(1)
                    .contains("[2行, 1列] : 項目「col_default」の値（abc）は、時刻の書式「HH:mm:ssxxx」として不正です。");                
        }
        
    }
}
