import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import scalariform.formatter.preferences._
import sbt._

object Formatting {
  lazy val formatSettings = Seq(
    ScalariformKeys.preferences in Compile  <<= formattingPreferences,
    ScalariformKeys.preferences in Test     <<= formattingPreferences
  )

  lazy val docFormatSettings = Seq(
    ScalariformKeys.preferences in Compile  <<= docFormattingPreferences,
    ScalariformKeys.preferences in Test     <<= docFormattingPreferences
  )

  def formattingPreferences = Def.setting {
    ScalariformKeys.preferences.value
      .setPreference(RewriteArrowSymbols, true)
      .setPreference(AlignParameters, true)
      .setPreference(AlignSingleLineCaseStatements, true)
      .setPreference(DanglingCloseParenthesis, Preserve)
      .setPreference(DoubleIndentClassDeclaration, false)
  }

  def docFormattingPreferences = Def.setting {
    ScalariformKeys.preferences.value
      .setPreference(RewriteArrowSymbols, false)
      .setPreference(AlignParameters, true)
      .setPreference(AlignSingleLineCaseStatements, true)
      .setPreference(DanglingCloseParenthesis, Preserve)
      .setPreference(DoubleIndentClassDeclaration, false)
  }
}
