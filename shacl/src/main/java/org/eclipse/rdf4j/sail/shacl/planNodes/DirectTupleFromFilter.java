/*******************************************************************************
 * Copyright (c) 2018 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/

package org.eclipse.rdf4j.sail.shacl.planNodes;


import org.apache.commons.lang.StringEscapeUtils;
import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.sail.SailException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Håvard Ottestad
 */
public class DirectTupleFromFilter implements PlanNode, PushBasedPlanNode, SupportsParentProvider {

	private boolean printed = false;

	private CloseableIteration<Tuple, SailException> parentIterator;

	Tuple next = null;
	private ParentProvider parentProvider;

	@Override
	public CloseableIteration<Tuple, SailException> iterator() {
		return new CloseableIteration<Tuple, SailException>() {

			private void calculateNext() {
				while (next == null && parentIterator.hasNext()) {
					parentIterator.next();
				}
			}

			@Override
			public void close() throws SailException {
				parentIterator.close();
			}

			@Override
			public boolean hasNext() throws SailException {
				calculateNext();
				return next != null;
			}

			@Override
			public Tuple next() throws SailException {
				calculateNext();
				Tuple temp = next;
				next = null;

				return temp;
			}


			@Override
			public void remove() throws SailException {

			}
		};
	}

	@Override
	public int depth() {
		return parentProvider.parent().stream().mapToInt(PlanNode::depth).sum()+2;
	}

	@Override
	public void getPlanAsGraphvizDot(StringBuilder stringBuilder) {
		if(printed) return;
		printed = true;
		stringBuilder.append(getId() + " [label=\"" + StringEscapeUtils.escapeJava(this.toString()) + "\"];").append("\n");

		if(parentProvider instanceof PlanNode){
			((PlanNode) parentProvider).getPlanAsGraphvizDot(stringBuilder);

		}

		if(parentProvider instanceof FilterPlanNode){
			((FilterPlanNode) parentProvider).getPlanAsGraphvizDot(stringBuilder);

		}
	}

	@Override
	public String toString() {
		return "DirectTupleFromFilter";
	}

	@Override
	public String getId() {
		return System.identityHashCode(this)+"";
	}

	@Override
	public IteratorData getIteratorDataType() {
		List<IteratorData> collect = parentProvider.parent().stream().map(PlanNode::getIteratorDataType).distinct().collect(Collectors.toList());
		if(collect.size() == 1) return collect.get(0);

		throw new IllegalStateException("Not implemented yet");
	}

	@Override
	public void push(Tuple t) {
		next = t;
	}

	@Override
	public void parentIterator(CloseableIteration<Tuple, SailException> iterator) {
		parentIterator = iterator;
	}


	@Override
	public void receiveParentProvider(ParentProvider parentProvider) {
		this.parentProvider = parentProvider;
	}
}
