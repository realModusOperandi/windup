package org.jboss.windup.reporting.rules;

import javax.inject.Inject;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.freemarker.FreeMarkerIterationOperation;
import org.jboss.windup.reporting.model.ApplicationReportModel;
import org.jboss.windup.reporting.model.TemplateType;
import org.jboss.windup.reporting.service.ReportModelService;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class ApplicationReportRenderingRuleProvider extends WindupRuleProvider
{
    private static final String APP_REPORTS_VAR = "applicationReportsIterable";
    private static final String APP_REPORT_VAR = "applicationReport";
    private static final String TEMPLATE_APPLICATION_REPORT = "/reports/templates/application.ftl";

    @Inject
    private ReportModelService reportModelService;

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.REPORT_RENDERING;
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        AbstractIterationOperation<ApplicationReportModel> setupTemplateOperation = new AbstractIterationOperation<ApplicationReportModel>(
                    ApplicationReportModel.class, APP_REPORT_VAR)
        {
            @Override
            public void perform(GraphRewrite event, EvaluationContext context, ApplicationReportModel payload)
            {
                payload.setTemplatePath(TEMPLATE_APPLICATION_REPORT);
                payload.setTemplateType(TemplateType.FREEMARKER);
                String applicationname = payload.getApplicationName();

                reportModelService.setUniqueFilename(payload, applicationname, "html");
            }
        };

        FreeMarkerIterationOperation reportOperation = FreeMarkerIterationOperation.create(APP_REPORT_VAR);

        return ConfigurationBuilder
            .begin()
            .addRule()
            .when(Query.find(ApplicationReportModel.class).as(APP_REPORTS_VAR))
            .perform(Iteration.over(APP_REPORTS_VAR).as(APP_REPORT_VAR)
                .perform(setupTemplateOperation.and(reportOperation))
                .endIteration());
    }
}