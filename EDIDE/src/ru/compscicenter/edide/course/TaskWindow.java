package ru.compscicenter.edide.course;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;

/**
 * User: lia
 * Date: 21.06.14
 * Time: 18:54
 * Implementation of windows which user should type in
 */


public class TaskWindow implements Comparable {

  public int line = 0;
  public int start = 0;
  public String text = "";
  public String hint = "";
  public String possibleAnswer = "";
  public int myLength = text.length();
  private TaskFile myTaskFile;
  public int myIndex = -1;
  public int myInitialLine = -1;
  public int myInitialStart = -1;
  public int myInitialLength = -1;
  private StudyStatus myStatus = StudyStatus.Unchecked;

  public StudyStatus getStatus() {
    return myStatus;
  }

  public void setStatus(StudyStatus status) {
    myStatus = status;
  }

  public void setIndex(int index) {
    myIndex = index;
  }

  public int getLength() {
    return myLength;
  }

  public void setLength(int length) {
    myLength = length;
  }

  public int getStart() {
    return start;
  }

  public void setStart(int start) {
    this.start = start;
  }

  public void setLine(int line) {
    this.line = line;
  }

  public int getLine() {
    return line;
  }


  /**
   * Draw task window with color according to its status
   */
  public void draw(Editor editor, boolean drawSelection, boolean moveCaret) {
    if (!isValid(editor.getDocument())) {
      return;
    }
    TextAttributes defaultTestAttributes =
      EditorColorsManager.getInstance().getGlobalScheme().getAttributes(EditorColors.LIVE_TEMPLATE_ATTRIBUTES);
    JBColor color = getColor();
    int startOffset = editor.getDocument().getLineStartOffset(line) + start;
    RangeHighlighter
      rh = editor.getMarkupModel().addRangeHighlighter(startOffset, startOffset + myLength, HighlighterLayer.LAST + 1,
                                                       new TextAttributes(defaultTestAttributes.getForegroundColor(),
                                                                          defaultTestAttributes.getBackgroundColor(), color,
                                                                          defaultTestAttributes.getEffectType(),
                                                                          defaultTestAttributes.getFontType()),
                                                       HighlighterTargetArea.EXACT_RANGE);
    if (drawSelection) {
      editor.getSelectionModel().setSelection(startOffset, startOffset + myLength);
    }
    if (moveCaret) {
      editor.getCaretModel().moveToOffset(startOffset);
    }
    rh.setGreedyToLeft(true);
    rh.setGreedyToRight(true);
  }

  private boolean isValid(Document document) {
    boolean isLineValid = line < document.getLineCount() && line >= 0;
    boolean isStartValid = start >=0 && start < document.getLineEndOffset(line);
    boolean isLengthValid = (getRealStartOffset(document) + myLength) < document.getTextLength();
    return isLengthValid && isStartValid && isLineValid;

  }

  private JBColor getColor() {
    if (myStatus == StudyStatus.Solved) {
      return JBColor.GREEN;
    }
    if (myStatus == StudyStatus.Failed) {
      return JBColor.RED;
    }
    return JBColor.BLUE;
  }

  public int getRealStartOffset(Document document) {
    return document.getLineStartOffset(line) + start;
  }

  /**
   * Initializes window
   *
   * @param file task file which window belongs to
   */
  public void init(TaskFile file, boolean isRestarted) {
    if (!isRestarted) {
      myInitialLine = line;
      myLength = text.length();
      myInitialLength = myLength;
      myInitialStart = start;
    }
    myTaskFile = file;
  }

  public TaskFile getTaskFile() {
    return myTaskFile;
  }

  @Override
  public int compareTo(@NotNull Object o) {
    TaskWindow taskWindow = (TaskWindow)o;
    if (taskWindow.getTaskFile() != myTaskFile) {
      throw new ClassCastException();
    }
    int lineDiff = line - taskWindow.line;
    if (lineDiff == 0) {
      return start - taskWindow.start;
    }
    return lineDiff;
  }

  /**
   * Returns window to its initial state
   */
  public void reset() {
    myStatus = StudyStatus.Unchecked;
    line = myInitialLine;
    start = myInitialStart;
    myLength = myInitialLength;
  }

  public String getHint() {
    return hint;
  }

  public String getPossibleAnswer() {
    return possibleAnswer;
  }

  public void setPossibleAnswer(String possibleAnswer) {
    this.possibleAnswer = possibleAnswer;
  }

  public int getIndex() {
    return myIndex;
  }
}