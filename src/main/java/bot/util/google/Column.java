package bot.util.google;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface Column {
	/** カラム番号 0から開始 */
	public int index();
	/** 背景色 デフォルト白 */
	public Color backgroundColor() default Color.WHITE;
}
