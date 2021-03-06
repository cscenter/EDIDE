package ru.compscicenter.edide.projectView;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleTextAttributes;
import icons.StudyIcons;
import org.jetbrains.annotations.NotNull;
import ru.compscicenter.edide.StudyTaskManager;
import ru.compscicenter.edide.StudyUtils;
import ru.compscicenter.edide.course.*;

import javax.swing.*;
import java.awt.*;

public class StudyDirectoryNode extends PsiDirectoryNode {
  private final PsiDirectory myValue;
  private final Project myProject;

  public StudyDirectoryNode(@NotNull final Project project,
                            PsiDirectory value,
                            ViewSettings viewSettings) {
    super(project, value, viewSettings);
    myValue = value;
    myProject = project;
  }

  @Override
  protected void updateImpl(PresentationData data) {
    data.setIcon(StudyIcons.UncheckedTask);
    String valueName = myValue.getName();
    StudyTaskManager studyTaskManager = StudyTaskManager.getInstance(myProject);
    Course course = studyTaskManager.getCourse();
    if (course == null) {
      return;
    }
    if (valueName.equals(myProject.getName())) {
      data.clearText();
      data.addText(course.getName(), new SimpleTextAttributes(Font.BOLD, JBColor.BLUE));
      data.addText(" (" + valueName + ")", SimpleTextAttributes.GRAYED_ATTRIBUTES);
      return;
    }
    if (valueName.contains(Task.TASK_DIR)) {
      TaskFile file = null;
      for (PsiElement child : myValue.getChildren()) {
        VirtualFile virtualFile = child.getContainingFile().getVirtualFile();
        file = studyTaskManager.getTaskFile(virtualFile);
        if (file != null) {
          break;
        }
      }
      if (file != null) {
        Task task = file.getTask();
        setStudyAttributes(task, data, task.getName());
      }
    }
    if (valueName.contains(Lesson.LESSON_DIR)) {
      int lessonIndex = Integer.parseInt(valueName.substring(Lesson.LESSON_DIR.length())) - 1;
      Lesson lesson = course.getLessons().get(lessonIndex);
      setStudyAttributes(lesson, data, lesson.getName());
    }

    if (valueName.contains(Course.PLAYGROUND_DIR)) {
      if (myValue.getParent() != null) {
        if (!myValue.getParent().getName().contains(Course.PLAYGROUND_DIR)) {
          data.setPresentableText(Course.PLAYGROUND_DIR);
          data.setIcon(StudyIcons.Playground);
          return;
        }
      }
    }
    data.setPresentableText(valueName);
  }

  @Override
  public int getTypeSortWeight(boolean sortByType) {
    String name = myValue.getName();
    if (name.contains(Lesson.LESSON_DIR) || name.contains(Task.TASK_DIR)) {
      String logicalName = name.contains(Lesson.LESSON_DIR) ? Lesson.LESSON_DIR : Task.TASK_DIR;
      return StudyUtils.getIndex(name, logicalName) + 1;
    }
    return name.contains(Course.PLAYGROUND_DIR) ? 0 : 3;
  }

  private void setStudyAttributes(Stateful stateful, PresentationData data, String additionalName) {
    StudyStatus taskStatus = stateful.getStatus();
    switch (taskStatus) {
      case Unchecked: {
        updatePresentation(data, additionalName, JBColor.blue, StudyIcons.UncheckedTask);
        break;
      }
      case Solved: {
        updatePresentation(data, additionalName, new JBColor(new Color(0, 134, 0), new Color(98, 150, 85)), StudyIcons.CheckedTask);
        break;
      }
      case Failed: {
        updatePresentation(data, additionalName, JBColor.RED, StudyIcons.FailedTask);
      }
    }
  }

  private void updatePresentation(PresentationData data, String additionalName, JBColor color, Icon icon) {
    data.clearText();
    data.addText(additionalName, new SimpleTextAttributes(Font.BOLD, color));
    data.setIcon(icon);
  }
}
