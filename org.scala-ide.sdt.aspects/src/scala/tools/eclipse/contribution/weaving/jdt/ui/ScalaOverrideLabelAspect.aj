/*
 * Copyright 2005-2010 LAMP/EPFL
 */
// $Id$

package scala.tools.eclipse.contribution.weaving.jdt.ui;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.OverrideIndicatorLabelDecorator;

@SuppressWarnings("restriction")
public privileged aspect ScalaOverrideLabelAspect {
  pointcut getOverrideIndicators(IMethod method) :
    args (method) &&
	  (execution(int OverrideIndicatorLabelDecorator.getOverrideIndicators(IMethod)));

  int around(IMethod method) throws JavaModelException : getOverrideIndicators(method) {
    if (method instanceof IMethodOverrideInfo) {
      return ((IMethodOverrideInfo)method).getOverrideInfo();
    } 
    else
      return proceed(method);
  }
}
