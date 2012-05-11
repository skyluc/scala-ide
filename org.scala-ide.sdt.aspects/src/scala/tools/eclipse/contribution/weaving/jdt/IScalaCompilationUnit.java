/*
 * Copyright 2005-2010 LAMP/EPFL
 */
// $Id$

package scala.tools.eclipse.contribution.weaving.jdt;

import java.util.Map;

import org.eclipse.jdt.internal.core.search.matching.MatchLocator;
import org.eclipse.jdt.internal.core.search.matching.PossibleMatch;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;

@SuppressWarnings("restriction")
public interface IScalaCompilationUnit {
  public IScalaWordFinder getScalaWordFinder();
  public void reportMatches(MatchLocator matchLocator, PossibleMatch possibleMatch);
  public void createOverrideIndicators(Map<Annotation, Position> annotationMap);
}
