/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mylyn project committers
 *******************************************************************************/
/*******************************************************************************
 * Copyright (c) 2008 Sven Krzyzak
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Sven Krzyzak - adapted Trac implementation for Redmine
 *******************************************************************************/

package org.svenk.redmine.ui.editor;

import static org.svenk.redmine.core.IRedmineConstants.TASK_ATTRIBUTE_TIMEENTRY;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil;
import org.eclipse.mylyn.tasks.core.IRepositoryPerson;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart;
import org.eclipse.mylyn.tasks.ui.editors.AttributeEditorToolkit;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.AbstractHyperlink;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.Section;
import org.svenk.redmine.core.RedmineAttribute;
import org.svenk.redmine.core.data.RedmineTaskTimeEntryMapper;
import org.svenk.redmine.ui.Images;

public class RedmineTimeEntryEditorPart extends AbstractTaskEditorPart {

	/** Expandable composites are indented by 6 pixels by default. */
	private static final int INDENT = -6;

	private boolean hasIncoming = false;
	
	private List<TaskAttribute> timeEntryAttributes;
	
	private Section section;
	
	public RedmineTimeEntryEditorPart() {
		super();
		setPartName(Messages.RedmineTimeEntryEditorPart_TIME_ENTRIES_SECTION_TITLE);
	}
	@Override
	public void createControl(Composite parent, final FormToolkit toolkit) {
		initialize();

		section = createSection(parent, toolkit, hasIncoming);

		StringBuilder nameDetail = new StringBuilder(section.getText());
		nameDetail.append(" (").append(timeEntryAttributes.size()); //$NON-NLS-1$
		
		TaskAttribute totalAttribute = getTaskData().getRoot().getAttribute(RedmineAttribute.TIME_ENTRY_TOTAL.getTaskKey());
		if(totalAttribute!=null && totalAttribute.getValue()!="") { //$NON-NLS-1$
			nameDetail.append(" - ").append(Messages.RedmineTimeEntryEditorPart_TIME_ENTRIES_TOTAL); //$NON-NLS-1$
			nameDetail.append(": ").append(totalAttribute.getValue()); //$NON-NLS-1$
			nameDetail.append(Messages.RedmineTimeEntryEditorPart_TIME_ENTRIES_HOURS);
		}
		
		nameDetail.append(")"); //$NON-NLS-1$
		section.setText(nameDetail.toString());
		
		if (timeEntryAttributes.isEmpty()) {
			section.setEnabled(false);
		} else {
			if (hasIncoming) {
				expandSection(toolkit, section);
			} else {
				section.addExpansionListener(new ExpansionAdapter() {
					@Override
					public void expansionStateChanged(ExpansionEvent event) {
						if (section.getClient() == null) {
							try {
//								expandAllInProgress = true;
								getTaskEditorPage().setReflow(false);

								expandSection(toolkit, section);
							} finally {
//								expandAllInProgress = false;
								getTaskEditorPage().setReflow(true);
							}
							getTaskEditorPage().reflow();
						}
					}
				});
			}
		}
		setSection(toolkit, section);

	}

	private void expandSection(final FormToolkit toolkit, final Section section) {
		Composite composite = toolkit.createComposite(section);
		section.setClient(composite);
		composite.setLayout(EditorUtil.createSectionClientLayout());

		List<TimeEntryViewer> viewers = getTimeEntryViewers();
		for (TimeEntryViewer viewer : viewers) {
			Control control = viewer.createControl(composite, toolkit);
			GridDataFactory.fillDefaults().grab(true, false).indent(INDENT, 0).applyTo(control);
		}
	}

	private List<TimeEntryViewer> getTimeEntryViewers() {
		
		List<TimeEntryViewer> viewers = new ArrayList<TimeEntryViewer>(timeEntryAttributes.size());
		for (TaskAttribute taskAttribute : timeEntryAttributes) {
			viewers.add(new TimeEntryViewer(taskAttribute));
		}
		
		return viewers;
	}
	
	private void initialize() {
		timeEntryAttributes = getTaskData().getAttributeMapper().getAttributesByType(getTaskData(), TASK_ATTRIBUTE_TIMEENTRY);
		if (timeEntryAttributes.size() > 0) {
			for (TaskAttribute timeEntryAttribute : timeEntryAttributes) {
				if (getModel().hasIncomingChanges(timeEntryAttribute)) {
					hasIncoming = true;
					break;
				}
			}
		}
	}
	
	private class TimeEntryViewer {
		
		private TaskAttribute attribute;
		
		private RedmineTaskTimeEntryMapper mapper;
		
		private ExpandableComposite timeEntryComposite;
		
		private boolean hasIncoming;
		
		private TimeEntryViewer(TaskAttribute timeEntryAttribute) {
			attribute = timeEntryAttribute;
			mapper = RedmineTaskTimeEntryMapper.createFrom(timeEntryAttribute);
			hasIncoming = getModel().hasIncomingChanges(timeEntryAttribute);
			
		}
		
		private Control createControl(final Composite composite, final FormToolkit toolkit) {
			Color bgColor = hasIncoming ? getTaskEditorPage().getAttributeEditorToolkit().getColorIncoming() : null;

//			Composite composite = toolkit.createComposite(parent);
//			GridLayout gl = new GridLayout(6, false);
//			composite.setLayout(gl);
//			GridData gd = new GridData();
//			gd.horizontalSpan = 4;
//			composite.setLayoutData(gd);

			int style = 
				ExpandableComposite.TREE_NODE 
				| ExpandableComposite.LEFT_TEXT_CLIENT_ALIGNMENT 
			| ExpandableComposite.COMPACT;

			timeEntryComposite = toolkit.createExpandableComposite(composite, style);
			timeEntryComposite.clientVerticalSpacing = 0;
			timeEntryComposite.setLayout(new GridLayout());
			timeEntryComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			timeEntryComposite.setTitleBarForeground(toolkit.getColors().getColor(IFormColors.TITLE));
			timeEntryComposite.setBackground(bgColor);
			
			
			final AbstractHyperlink titleLink = createTitle(timeEntryComposite, toolkit, bgColor);

			final Composite detailsComposite = toolkit.createComposite(timeEntryComposite);
			timeEntryComposite.setClient(detailsComposite);

			GridLayout gl = new GridLayout(6, false);
			detailsComposite.setLayout(gl);
			GridData gd = new GridData();
			gd.horizontalSpan = 4;
			detailsComposite.setLayoutData(gd);

			/* Expand: Click on caret */
			timeEntryComposite.addExpansionListener(new ExpansionAdapter() {
				@Override
				public void expansionStateChanged(ExpansionEvent event) {
					expandTimeEntry(toolkit, detailsComposite, event.getState());
				}
			});

			/* Expand: Click on title */
			titleLink.addHyperlinkListener(new HyperlinkAdapter() {
				@Override
				public void linkActivated(HyperlinkEvent e) {
					boolean expand = !timeEntryComposite.isExpanded();
					timeEntryComposite.setExpanded(expand);
					expandTimeEntry(toolkit, detailsComposite, expand);
				}
			});

			if(hasIncoming && !timeEntryComposite.isExpanded()) {
				timeEntryComposite.setExpanded(true);
				expandTimeEntry(toolkit, detailsComposite, true);
			}
			
			return composite;
		}

		private AbstractHyperlink createTitle(final ExpandableComposite timeEntryComposite, final FormToolkit toolkit, Color bgColor) {
			// always visible
			Composite titleComposite = toolkit.createComposite(timeEntryComposite);
			timeEntryComposite.setTextClient(titleComposite);
			RowLayout rowLayout = new RowLayout();
			rowLayout.pack = true;
			rowLayout.marginLeft = 0;
			rowLayout.marginBottom = 0;
			rowLayout.marginTop = 0;
			EditorUtil.center(rowLayout);
			titleComposite.setLayout(rowLayout);
			titleComposite.setBackground(null);
			
			ImageHyperlink expandCommentHyperlink = createTitleHyperLink(toolkit, titleComposite);
			expandCommentHyperlink.setFont(timeEntryComposite.getFont());
//
//			// only visible when section is expanded
//			final Composite buttonComposite = toolkit.createComposite(titleComposite);
//			RowLayout buttonCompLayout = new RowLayout();
//			buttonCompLayout.marginBottom = 0;
//			buttonCompLayout.marginTop = 0;
//			buttonComposite.setLayout(buttonCompLayout);
//			buttonComposite.setBackground(null);
//			buttonComposite.setVisible(commentComposite.isExpanded());
//
//			ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
//			ReplyToCommentAction replyAction = new ReplyToCommentAction(this, taskComment);
//			replyAction.setImageDescriptor(TasksUiImages.COMMENT_REPLY_SMALL);
//			toolBarManager.add(replyAction);
//			toolBarManager.createControl(buttonComposite);

			//Hours
			AttributeEditorToolkit editorToolkit = getTaskEditorPage().getAttributeEditorToolkit();
			AbstractAttributeEditor attributeEditor = createAttributeEditor(RedmineTaskTimeEntryMapper.getHoursAttribute(attribute));
			attributeEditor.createLabelControl(titleComposite, toolkit);
			attributeEditor.createControl(titleComposite, toolkit);
			attributeEditor.getLabelControl().setBackground(bgColor);
			editorToolkit.adapt(attributeEditor);
			
			//Activity
			attributeEditor = createAttributeEditor(RedmineTaskTimeEntryMapper.getActivityAttribute(attribute));
			attributeEditor.createLabelControl(titleComposite, toolkit);
			attributeEditor.createControl(titleComposite, toolkit);
			attributeEditor.getLabelControl().setBackground(bgColor);
			editorToolkit.adapt(attributeEditor);
			
			return expandCommentHyperlink;
		}

		private ImageHyperlink createTitleHyperLink(final FormToolkit toolkit, final Composite toolbarComp) {
			ImageHyperlink formHyperlink = toolkit.createImageHyperlink(toolbarComp, SWT.NONE);
			formHyperlink.setBackground(null);
			formHyperlink.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
			StringBuilder sb = new StringBuilder();

			//Author
			TaskAttribute authorAttribute = RedmineTaskTimeEntryMapper.getAuthorAttribute(attribute);
			if(authorAttribute != null) {
				TaskAttributeMapper attributeMapper = attribute.getTaskData().getAttributeMapper();
				IRepositoryPerson author = attributeMapper.getRepositoryPerson(authorAttribute);
				formHyperlink.setImage(Images.getImage(Images.PERSON_NARROW));
				if (author != null) {
					if (author.getName() != null) {
						sb.append(author.getName());
					} else {
						sb.append(author.getPersonId());
					}
				}
			}
			
			//Date
			if (mapper.getSpentOn()!=null) {
				if(sb.length()>0) {
					sb.append(", "); //$NON-NLS-1$
				}
				sb.append(DateFormat.getDateInstance(DateFormat.MEDIUM).format(mapper.getSpentOn()));
			}
			
			formHyperlink.setText(sb.toString());
			formHyperlink.setEnabled(true);
			formHyperlink.setUnderlined(false);
			return formHyperlink;
		}

		private void expandTimeEntry(FormToolkit toolkit, Composite composite, boolean expanded) {
//			buttonComposite.setVisible(expanded);
			if (expanded) {
				// create viewer
				
				AttributeEditorToolkit editorToolkit = getTaskEditorPage().getAttributeEditorToolkit();
				TaskAttribute taskAttribute = RedmineTaskTimeEntryMapper.getCommentsAttribute(attribute);
				AbstractAttributeEditor editor = createAttributeEditor(taskAttribute);
				if (editor != null) {
					editor.createLabelControl(composite, toolkit);
					editor.createControl(composite, toolkit);
					
//					editor.getControl().addMouseListener(new MouseAdapter() {
//						@Override
//						public void mouseDown(MouseEvent e) {
//							getTaskEditorPage().selectionChanged(taskComment);
//						}
//					});
					

					editorToolkit.adapt(editor);
					getTaskEditorPage().reflow();
				}
				
				Collection<TaskAttribute> customAttributes = RedmineTaskTimeEntryMapper.getCustomAttributes(attribute);
				if (customAttributes!=null) {
					for (TaskAttribute customAttribute : customAttributes) {
						editor = createAttributeEditor(customAttribute);
						if (editor != null) {
							editor.createLabelControl(composite, toolkit);
							editor.getLabelControl().setText(editor.getLabel() + ":"); //$NON-NLS-1$
							editor.createControl(composite, toolkit);
							
							editorToolkit.adapt(editor);
							getTaskEditorPage().reflow();
						}
					}
				}
				
			} else if (!expanded /* && composite.getData(KEY_EDITOR) != null */) {
//				// dispose viewer
				
				Control[] childElems = composite.getChildren();
				for (Control childElem : childElems) {
					childElem.dispose();
				}
				
//				AbstractAttributeEditor editor = (AbstractAttributeEditor) composite.getData(KEY_EDITOR);
////				editor.getControl().setMenu(null);

				getTaskEditorPage().reflow();
			}
//			getTaskEditorPage().selectionChanged(taskComment);
		}

	}

}
