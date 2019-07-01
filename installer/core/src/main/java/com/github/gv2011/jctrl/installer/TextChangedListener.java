package com.github.gv2011.jctrl.installer;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public interface TextChangedListener extends DocumentListener{

  @Override
  default void insertUpdate(final DocumentEvent e) {
    textChanged();
  }

  @Override
  default void removeUpdate(final DocumentEvent e) {
    textChanged();
  }

  @Override
  default void changedUpdate(final DocumentEvent e) {
    textChanged();
  }

  void textChanged();

}
