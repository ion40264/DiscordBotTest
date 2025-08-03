
package jp.highwide.common.excel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface ExcelColumn {
	public static final String NON = "jp.highwide.common.excel.ExcelColumn.NONE";

	/** カラム名 */
	String columnName() default NON;

	/** カラム番号 0から開始 */
	int columnIndex();

	/** Pkey */
	boolean pkey() default false;
}