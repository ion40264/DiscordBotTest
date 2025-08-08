
package jp.highwide.common.excel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ExcelEntity {
	/** スタイルのenum */
	public static enum Align {
		ALIGN_LEFT, ALIGN_CENTER, ALIGN_RIGHT
	};

	public static enum Border {
		NONE, STYLE1, STYLE2, STYLE3
	}

	/** ヘッダ true:あり false:なし */
	boolean header() default true;

	Align headerStyle() default Align.ALIGN_LEFT;

	Border borderStyle() default Border.NONE;
}
