Deviation Investigation Workflow

Step 1: Deviation Type Detection
Objective:
To classify the deviation based solely on the provided description into one of three types: OOS, OOT, or Yield Deviation.
Revised Prompt:
"Given the following deviation description:
[Insert deviation description here]
Classify this deviation as one of the following types (only these three options are allowed):
OOS (Out of Specification): The result falls outside established specification limits.
OOT (Out of Trend): The result shows unusual trends or variability beyond acceptable limits.
Yield Deviation: The process yield is outside the acceptable range defined by product yield standards.
Please provide your output in the following structured format (JSON):

{
  "primary_deviation_type": "<OOS | OOT | Yield Deviation>",
  "confidence": "<HIGH | MEDIUM | LOW>",
  "reasoning": "<Detailed explanation for the classification, based solely on the description provided>"
}

Notes:
Base your classification strictly on the description provided, without reference to any external validation or data (which will be handled in subsequent steps).
If the description is ambiguous or lacks sufficient detail, please indicate this in your reasoning and assign an appropriate (e.g., lower) confidence level."
Step 2: Initial Information Extraction (Unified Format)
Objective:
Using the previously determined deviation type from Step 1, extract and structure all other relevant details from the deviation description into the unified JSON schema. This schema covers product, measurement, and context information in a standardized way.
Unified JSON Schema Template:
{
  "deviation_type": "OOS | OOT | Yield Deviation", // Already determined in Step 1; one of these three values.
  "product_information": {
    "product_code": { 
      "value": "",          // Extract the product code or name.
      "confidence": ""      // Confidence level: HIGH, MEDIUM, or LOW.
    },
    "batch_number": { 
      "value": "",          // Extract the batch number.
      "confidence": ""      // Confidence level.
    },
    "manufacturing_stage": { 
      "value": "",          // Extract the manufacturing stage (e.g., Dissolution Testing, Packaging, etc.).
      "confidence": ""      // Confidence level.
    },
    "batch_size": { 
      "value": "",          // Extract the batch size if applicable (primarily for Yield Deviations); otherwise, leave blank.
      "confidence": ""      // Confidence level.
    }
  },
  "measurement_information": {
    "measurement_parameter": { 
      "value": "",          // Extract the parameter being tested (e.g., Dissolution, Yield).
      "confidence": ""      // Confidence level.
    },
    "measurement_method": { 
      "value": "",          // Extract the test method or equipment used.
      "confidence": ""      // Confidence level.
    },
    "observed_value": { 
      "value": "",          // Extract the observed test result or yield value.
      "confidence": ""      // Confidence level.
    },
    "expected_value": { 
      "value": "",          // For OOS: specification limit; OOT: trend/control limit; Yield: expected yield range.
      "confidence": ""      // Confidence level.
    },
    "unit": { 
      "value": "",          // Extract the unit of measurement (e.g., %).
      "confidence": ""      // Confidence level.
    },
    "additional_details": { 
      "value": "",          // Any extra details relevant to the deviation type (e.g., trend variability, yield calculation specifics).
      "confidence": ""      // Confidence level.
    }
  },
  "context_information": {
    "timestamp": { 
      "value": "",          // Extract the timestamp when the deviation was observed or test performed.
      "confidence": ""      // Confidence level.
    },
    "operator": { 
      "value": "",          // Extract the name of the operator/tester who performed the test.
      "confidence": ""      // Confidence level.
    },
    "location": { 
      "value": "",          // Extract the location where the event occurred (e.g., facility or lab unit).
      "confidence": ""      // Confidence level.
    },
    "observations": { 
      "value": "",          // Extract any additional observations noted in the deviation description.
      "confidence": ""      // Confidence level.
    }
  }
}
Extraction Prompt:
"Given the deviation description below and knowing that the deviation type has already been classified in Step 1 as [OOS | OOT | Yield Deviation], please extract the following information into the unified JSON format:
Instructions:
Base your extraction solely on the provided deviation description.
Use the unified key names exactly as shown, and include a 'confidence' level (HIGH, MEDIUM, LOW) for each extracted element.
Do not re-classify the deviation type; simply include the already determined value.
Deviation Description:
[Insert deviation description here]
Output Format:
Provide your answer in the JSON structure given in the unified template."
Step 3: Deviation Validation
Objective:
To verify that the quality event (extracted in Step 2) represents a genuine deviation by comparing its details against the established product specifications, trend rules, or yield standards. In other words, confirm that the observed values deviate from the “truth” as defined by our validated data sources.
What This Step Does:
Cross-Reference with Authoritative Data:
The extracted information (e.g., product code, batch number, measured parameter, observed value, and context details) is validated by running specific database queries. These queries retrieve:
Product and Parameter Existence: Confirm that the product and its corresponding parameter are valid.
Specification Limits (for OOS): Retrieve the accepted specification limits for the parameter.
Trend Rules (for OOT): Retrieve the statistical control limits and trend rules.
Yield Standards (for Yield Deviations): Retrieve the expected yield ranges, stage-to-stage relationships, and historical yield data.
Comparison & Decision Making:
With the retrieved data, the system compares:
The observed value from the quality event against the expected value from the database.
Any additional details (e.g., test conditions or calculation methods) to check if the deviation is valid according to established rules.
Output:
The result is a validation report that includes:
Whether the quality event meets the criteria for a true deviation.
Specific rules or limits that were exceeded (or not).
Confidence levels and supporting evidence from the queries.
Structured Prompt for Deviation Validation:
"Using the extracted deviation information provided below, and referencing the authoritative product data, please validate if this quality event is a genuine deviation.
Extracted Deviation Information (from Step 2):
[Insert unified JSON from Step 2]
Validation Data Sources:
Product & Parameter Existence:
Query: Validate that the product (using product_code) and the test parameter exist in the product parameters database.
For OOS Deviations:
Query: Retrieve specification limits from the 'parameter_limits' table for the given product and parameter.
Query: Retrieve test method requirements from the 'test_methods' table.
For OOT Deviations:
Query: Retrieve trend rules and statistical control limits from the 'trend_rules' and 'statistical_limits' tables.
For Yield Deviations:
Query: Retrieve yield standards from the 'product_yield_standards' table.
Query: Retrieve stage-to-stage relationships and historical yield data.
Instructions:
Compare the observed_value in the extracted information with the expected_value (or relevant standard) obtained from the queries.
Confirm whether the deviation criteria (specification limit breach, trend rule trigger, or yield standard deviation) are met.
Provide a structured validation report in JSON format with the following keys:
validation_result: "Confirmed" or "Not Confirmed" (with explanation).
triggered_rules: List the specific rules or limits that were exceeded.
confidence: Overall confidence level in the validation (HIGH, MEDIUM, LOW).
supporting_evidence: Brief notes on query results that support the conclusion.
Output Example:
{
  "validation_result": "Confirmed",
  "triggered_rules": ["Specification limit of 85% exceeded", "Test method requirement not met"],
  "confidence": "HIGH",
  "supporting_evidence": "Product parameter exists; observed value 80% is below the 85% specification limit."
}

Base your validation solely on the comparison of the extracted information and the data retrieved from the queries. Do not assume additional context beyond what is provided.


Step 4: Impact Assessment (Revised with Product Master Data)
Objective:
To evaluate the impact of the detected deviation by comparing the observed event against both the product’s master specifications (from the detailed documents) and historical deviation data (via SQL queries). This step determines the quality, manufacturing, and batch impacts by merging authoritative product/process knowledge with historical performance and impact trends.

1. Data Gathering
A. Product Master Knowledge Extraction
Retrieve relevant sections from the master document that include:
Product Overview and Quality Requirements:
Specification limits, quality attributes, and critical quality parameters (e.g., dissolution limits, tablet weight range, hardness, friability, etc.).
Manufacturing Process Details:
Process flow, critical process parameters, equipment capacities, and stage-wise requirements.
Testing Requirements:
Test methods, sampling plans, and acceptance criteria.
For example, extract key data points such as:
Product Name: METFORMIN TBL. 850 MG NEW FORM
Specification limits (e.g., tablet weight target, dissolution requirements, etc.)
Critical process parameters (e.g., mixing times, granulation temperature ranges)
Equipment used and stage-specific requirements.
B. Historical Deviation Data via SQL Queries
Run queries to retrieve:
Historical Pattern Data:
Retrieve recent deviation records and associated impact details for the current product, deviation type, and manufacturing stage.
Parameter Relationships & Validation Context:
Identify related parameters, proven operating ranges, and any established relationships affecting impact.
Impact Patterns:
Retrieve known impact patterns (frequency, typical impact areas) from the historical data.
Example SQL Queries:
-- Historical Pattern Data
SELECT 
    d.deviation_number,
    d.detection_date,
    d.criticality,
    hi.impact_area,
    hi.impact_details
FROM deviation_records d
JOIN historical_impacts hi ON d.id = hi.deviation_record_id
WHERE 
    d.process_stage = $CURRENT_STAGE
    AND d.deviation_type = $DEVIATION_TYPE
    AND d.product_id = $PRODUCT_ID
ORDER BY d.detection_date DESC
LIMIT 5;

sql
Copy
-- Parameter Relationships and Validation Context
SELECT 
    pp.parameter_name,
    pl.limits,
    vk.proven_range,
    pr.related_parameters
FROM product_parameters pp
JOIN parameter_limits pl ON pp.id = pl.product_parameter_id
LEFT JOIN validation_knowledge vk ON pp.id = vk.parameter_id
LEFT JOIN parameter_relationships pr ON pp.product_id = pr.product_id AND pp.parameter_name = pr.parameter_name
WHERE 
    pp.product_id = $PRODUCT_ID
    AND (pp.parameter_name = $CURRENT_PARAMETER OR pp.id IN (
          SELECT DISTINCT pp2.id 
          FROM parameter_relationships pr2
          JOIN product_parameters pp2 ON pr2.product_id = pp2.product_id
          WHERE pr2.parameter_name = $CURRENT_PARAMETER
    ));

sql
Copy
-- Impact Patterns
SELECT 
    ip.impact_area,
    ip.pattern_data,
    ip.frequency
FROM impact_patterns ip
WHERE 
    ip.process_stage = $CURRENT_STAGE
    AND ip.deviation_type = $DEVIATION_TYPE
    AND (ip.parameter_id = $PARAMETER_ID OR ip.parameter_id IS NULL);


2. LLM Impact Assessment Prompt
Using the gathered data, craft a prompt that instructs the LLM to integrate both data sources into a structured impact assessment report.
LLM Prompt:
"Using the following inputs, perform a comprehensive impact assessment for the deviation:
A. Extracted Deviation Information (Unified JSON from Step 2):
[Insert unified JSON from Step 2]
B. Product Master Data (from the provided product document):
Extract key details such as product specifications, quality requirements, process parameters, and testing criteria.
C. Historical Deviation Data:
Historical Patterns:
[Insert results from Historical Pattern Data SQL query]
Parameter Relationships & Validation Context:
[Insert results from the Parameter Relationships query]
Impact Patterns:
[Insert results from the Impact Patterns query]
Instructions:
Quality Impact Assessment:
Evaluate how the deviation affects product quality by comparing the observed value to the master specifications (e.g., dissolution limits, tablet weight ranges) and historical cases.
Provide supporting evidence drawn from both the product master data and historical deviation data.
Manufacturing Impact Assessment:
Assess the potential impact on the manufacturing process, including process stage performance, equipment stress, and any disruptions compared to established process parameters from the master document.
Support your evaluation with historical trends and known impact patterns.
Batch Impact Assessment:
Analyze the potential overall effect on the batch, including yield deviations and compliance issues.
Compare the observed deviation against historical batch performance data and master batch size/quality metrics.
Overall Recommendations & Verification:
Provide clear recommendations for further verification or corrective actions if needed.
Output Format:
Structure your output in JSON format with the following keys:
quality_impact: { "evaluation": "<text>", "confidence": "<HIGH | MEDIUM | LOW>", "supporting_evidence": "<text>" }
manufacturing_impact: { "evaluation": "<text>", "confidence": "<HIGH | MEDIUM | LOW>", "supporting_evidence": "<text>" }
batch_impact: { "evaluation": "<text>", "confidence": "<HIGH | MEDIUM | LOW>", "supporting_evidence": "<text>" }
recommendations: "<text>"
overall_confidence: "<HIGH | MEDIUM | LOW>"
Example Output:
json
Copy
{
  "quality_impact": {
    "evaluation": "The observed dissolution value is significantly below the master specification of 85%, which aligns with historical cases of quality failure.",
    "confidence": "HIGH",
    "supporting_evidence": "Product document specifies a minimum of 85% dissolution; historical records show similar deviations result in quality issues."
  },
  "manufacturing_impact": {
    "evaluation": "The deviation may lead to process delays and equipment stress, as the current manufacturing parameters are not met.",
    "confidence": "MEDIUM",
    "supporting_evidence": "Historical impact patterns indicate similar deviations cause disruptions in tablet compression and coating stages."
  },
  "batch_impact": {
    "evaluation": "The overall batch yield is likely compromised, potentially leading to a need for reprocessing or further batch testing.",
    "confidence": "HIGH",
    "supporting_evidence": "Master batch size data and historical yield trends suggest that deviations of this magnitude affect overall batch performance."
  },
  "recommendations": "Review equipment calibration, revalidate process parameters, and perform additional batch sampling for confirmation.",
  "overall_confidence": "HIGH"
}

Base your evaluation on the integration of master product data and historical deviation data. If any key data is ambiguous or missing, note that in your recommendations and adjust the confidence levels accordingly."

3. Handling Ambiguities & Confidence
Integration Strategy:
Merge the product master data (which is static and highly detailed) with dynamic historical data to identify common trends.
Use the master document as the definitive guide for product specifications and process expectations.
Adjust confidence levels if the historical data conflicts with the master information.
Fallback:
If any critical piece of historical data is missing or inconsistent, flag that area and recommend manual review.
Clearly indicate where uncertainties exist.

Final Output
The final output of this step should be a JSON object structured as shown in the prompt example. This comprehensive impact assessment report will serve as the input for the subsequent Severity Assessment step.
Step 5: Severity Assessment
Objective:
Determine the severity level of the deviation (CRITICAL, MAJOR, or MINOR) based on the impact assessment, comparing the deviation against master product/process specifications and historical deviation cases.

1. Data Sources
A. Impact Assessment Output:
This includes the quality, manufacturing, and batch impact evaluations from Step 4, with supporting evidence and confidence levels.
B. Master Product Data (from the product document):
Extracted critical SOP-based criteria (e.g., specification limits, process windows, and quality requirements).
C. Historical Deviation Data (via SQL queries):
Recent deviation records with their severity classifications, key similarities/differences, and historical rationale for classification.

2. LLM Severity Assessment Prompt
Use the following prompt to instruct the LLM to integrate the above data sources and provide a structured severity assessment in JSON format.
"Using the inputs below, please perform a severity assessment for the deviation:
A. Impact Assessment Output:
[Insert JSON output from the Impact Assessment step]
B. Master Product Data (from the product document):
Extract relevant SOP criteria, process windows, and quality requirements from the master document.
C. Historical Deviation Data:
Historical Severity Trends:
[Insert recent deviation records with severity classifications from SQL queries]
SOP-based Classification Criteria:
Extract key criteria as outlined in the product’s quality requirements.
Instructions:
Criteria Evaluation:
Evaluate how the observed deviation aligns with the master product specifications and quality requirements.
Identify which criteria are exceeded (or nearly exceeded) based on the impact data and master information.
Historical Comparison:
Compare the current deviation to similar historical cases.
Note any patterns or differences that might affect the severity classification.
Classification Decision:
Based on the above, recommend a severity level: 'CRITICAL', 'MAJOR', or 'MINOR'.
Provide a detailed rationale that includes supporting evidence from both the master data and historical trends.
Confidence Assessment:
Provide an overall confidence level for the severity classification (HIGH, MEDIUM, LOW).
Explain any uncertainties or additional factors to consider.
Additional Considerations:
Highlight any unique aspects of the current deviation that might not be fully covered by standard criteria.
Suggest if further data or manual review is needed.
Output Format:
Please return your assessment in the following JSON structure:
{
  "severity_assessment": {
    "criteria_evaluation": {
      "matching_criteria": [ "List specific criteria exceeded (e.g., dissolution value below spec, yield lower than standard)" ],
      "non_matching_criteria": [ "List criteria that were almost met or are ambiguous" ],
      "evidence": "Summarize the key evidence from master data and impact assessment"
    },
    "historical_comparison": {
      "similar_cases": "Summary of similar historical deviations and their severities",
      "observations": "Key similarities or differences that inform the decision"
    },
    "classification_decision": {
      "recommended_severity": "CRITICAL | MAJOR | MINOR",
      "rationale": "Detailed reasoning for the classification decision"
    },
    "confidence_assessment": {
      "confidence_level": "HIGH | MEDIUM | LOW",
      "uncertainties": "Any factors contributing to uncertainty"
    },
    "additional_considerations": "Any extra notes, suggestions for further review, or unique aspects of the deviation"
  }
}

Base your evaluation strictly on the provided inputs, integrating both the master product data and historical deviation data. If any critical data is ambiguous or missing, clearly note this in your output and adjust the confidence level accordingly."

3. Inline JSON Template with Comments
To help guide the LLM and ensure consistency, here’s a commented JSON template:
{
  "severity_assessment": {
    "criteria_evaluation": {
      "matching_criteria": [ 
        // List specific criteria that the deviation exceeds (e.g., "Dissolution value 75% is below the minimum 85% spec")
      ],
      "non_matching_criteria": [
        // List criteria that are borderline or not clearly exceeded
      ],
      "evidence": "" // Summarize evidence from master product data and impact assessment (e.g., "Observed yield 88% vs. expected 90-96%, with similar historical cases classified as MAJOR")
    },
    "historical_comparison": {
      "similar_cases": "", // Summarize similar cases from historical deviation data
      "observations": ""   // Note key similarities or differences that impact severity
    },
    "classification_decision": {
      "recommended_severity": "", // One of: "CRITICAL", "MAJOR", or "MINOR"
      "rationale": ""             // Detailed explanation of the decision
    },
    "confidence_assessment": {
      "confidence_level": "", // Overall confidence level: HIGH, MEDIUM, or LOW
      "uncertainties": ""     // Explain any uncertainties or areas needing further review
    },
    "additional_considerations": "" // Any extra notes or recommendations
  }
}





Factor Identification Sub-Step
Objective:
Automatically identify potential contributing factors for the deviation under each Ishikawa category. For each category, the system will:
List potential factors that might have contributed to the deviation.
Assign a confidence level (HIGH, MEDIUM, LOW) to each factor.
Provide a brief rationale explaining why the factor is considered relevant.
Data Sources to Leverage:
Current Deviation Context:
Extracted unified JSON from previous steps (product, measurement, context information).
Master Product Data:
Detailed product/process specifications, quality requirements, and process parameters from the product documentation.
Historical Deviation Data:
Past deviation records, associated root causes, and impact trends that may indicate recurring issues.

Instructions for the LLM (Factor Identification Prompt):
"Using the provided information, please identify potential contributing factors for the quality deviation by mapping them to the six Ishikawa categories:
Categories to Consider:
People: Factors related to personnel—operator errors, training issues, communication gaps, or fatigue.
Methods: Factors related to processes and procedures—SOP deviations, unclear process instructions, or outdated methods.
Machines: Factors related to equipment—malfunctions, calibration issues, maintenance problems, or incorrect machine settings.
Materials: Factors related to raw materials or components—quality variations, supplier inconsistencies, improper storage, or specification mismatches.
Measurements: Factors related to data and testing systems—inaccurate measurements, instrument calibration errors, or sampling mistakes.
Environment: Factors related to physical or operational conditions—temperature, humidity, contamination, or inadequate environmental controls.
Input Data Provided:
Unified Deviation Information: [Insert unified JSON from Step 2]
Master Product Data: Key product/process specifications from the product document.
Historical Deviation Data: Relevant records or trends from past deviations.
Task:
For each of the six categories, please list potential contributing factors for the current deviation. For each factor, provide:
Factor Description: A clear description of the potential contributing factor.
Confidence Level: HIGH, MEDIUM, or LOW, based on how strongly the data supports this factor.
Rationale: A brief explanation summarizing why this factor is relevant (e.g., alignment with master product specifications, historical trends, or context clues from the deviation).
Output Format:
Provide your output as a JSON object using the following structure:
{
  "ishikawa_factors": {
    "People": [
      {
        "factor": "",          // e.g., "Operator error due to inadequate training"
        "confidence": "",      // HIGH, MEDIUM, or LOW
        "rationale": ""        // Brief explanation of why this factor is considered
      }
      // ... additional factors under People
    ],
    "Methods": [
      {
        "factor": "",          // e.g., "Deviation from standard operating procedures"
        "confidence": "",
        "rationale": ""
      }
    ],
    "Machines": [
      {
        "factor": "",          // e.g., "Equipment calibration failure or maintenance lapse"
        "confidence": "",
        "rationale": ""
      }
    ],
    "Materials": [
      {
        "factor": "",          // e.g., "Variability in raw material quality from suppliers"
        "confidence": "",
        "rationale": ""
      }
    ],
    "Measurements": [
      {
        "factor": "",          // e.g., "Inaccurate measurement due to instrument error"
        "confidence": "",
        "rationale": ""
      }
    ],
    "Environment": [
      {
        "factor": "",          // e.g., "Inadequate environmental controls affecting process performance"
        "confidence": "",
        "rationale": ""
      }
    ]
  }
}
Notes:
Base your factor identification on the integration of the unified deviation information, the master product documentation (which outlines the expected process, quality, and testing criteria), and historical deviation trends.
Ensure that each factor is clearly linked to one of the six categories.
If certain categories yield no clear factors, you may return an empty array for that category.
Provide your structured response in JSON."

Explanation:
People: Look for any indications of operator mistakes, lack of training, or communication issues.
Methods: Identify any discrepancies between the executed process and the documented SOPs.
Machines: Check for potential issues with equipment performance, calibration logs, or maintenance history.
Materials: Determine if variations in raw materials or handling issues might have contributed.
Measurements: Examine if the measurement instruments or data collection methods might have been flawed.
Environment: Evaluate if environmental conditions (e.g., temperature, humidity) deviated from the required settings.
Investigative Task Generation Sub-Step
Objective:
For every identified contributing factor (from the Ishikawa Factor Identification sub-step), generate a specific investigative task. Each task should direct the investigation team on what evidence to collect, what records to review, and how to assess the factor's contribution to the deviation.
What This Sub-Step Does:
Takes Each Identified Factor:
For each factor listed under the six Ishikawa categories (People, Methods, Machines, Materials, Measurements, Environment), the system creates a corresponding Zinvestigative task.
Creates Clear, Actionable Tasks:
Each task includes:
Task Description: A concise statement of what needs to be investigated (e.g., "Review operator training records").
Evidence/Records to Collect: A list of documents, logs, or data sources (e.g., training logs, maintenance records, SOP documents).
Detailed Instructions: Guidance on how to perform the investigation (e.g., "Check if the equipment's calibration date is overdue").
Rationale: A brief explanation of why this task is relevant to confirming or refuting the factor's influence.
Initial Confidence: An assigned confidence level (HIGH, MEDIUM, or LOW) reflecting the strength of the supporting evidence from the factor identification stage.
Maintains a Consistent Format:
The output is structured in a unified JSON format, making it easier for downstream processes and human reviewers to follow.

LLM Prompt for Investigative Task Generation:
"Using the identified factors from the Ishikawa Factor Identification (provided as a JSON object with factors categorized under People, Methods, Machines, Materials, Measurements, and Environment), please generate a list of investigative tasks for each factor.
For each factor, include the following information:
Task Description: A clear and actionable statement on what to investigate regarding this factor.
Evidence/Records to Collect: A list of documents, logs, or data sources to review (e.g., training records, equipment maintenance logs, SOP documents).
Detailed Instructions: Guidance on how to perform the investigation (e.g., "Check for overdue calibration dates" or "Interview the operator about recent training sessions").
Rationale: A brief explanation of how this task will help determine if the factor contributed to the deviation.
Initial Confidence: Based on the factor's previously assigned confidence, indicate an initial confidence level for the task (HIGH, MEDIUM, or LOW).
Input:
Ishikawa Factors JSON: [Insert JSON output from the Factor Identification sub-step]
Output Format:
Please structure your output as a JSON object with keys corresponding to the Ishikawa categories. For example:
{
  "investigative_tasks": {
    "People": [
      {
        "task_description": "Review operator training records and recent attendance logs.",
        "evidence_required": [
          "Operator training records",
          "Shift attendance logs",
          "Interview notes from operator debriefs"
        ],
        "instructions": "Compare the recorded training dates and qualifications with the expected training schedule as defined in the master product documentation. Identify any discrepancies or gaps that could contribute to the deviation.",
        "rationale": "This task verifies if operator error due to inadequate training might have contributed to the deviation.",
        "initial_confidence": "HIGH"
      }
      // ... additional tasks for People factors
    ],
    "Methods": [
      {
        "task_description": "Review SOP documents and process execution records.",
        "evidence_required": [
          "Standard Operating Procedures (SOP) documents",
          "Process execution logs",
          "Deviation reports referencing procedural non-compliance"
        ],
        "instructions": "Examine if the actual process steps followed align with the documented SOPs. Identify any deviations or procedural lapses.",
        "rationale": "This helps determine if non-compliance with established methods contributed to the deviation.",
        "initial_confidence": "MEDIUM"
      }
      // ... additional tasks for Methods factors
    ],
    "Machines": [
      {
        "task_description": "Inspect equipment calibration and maintenance logs for the relevant test device.",
        "evidence_required": [
          "Calibration logs",
          "Maintenance records",
          "Equipment performance reports"
        ],
        "instructions": "Verify that the equipment was calibrated and maintained per the manufacturer's guidelines and master documentation. Look for any signs of malfunction or overdue maintenance.",
        "rationale": "Proper equipment functioning is critical; any issues here may lead to inaccurate results.",
        "initial_confidence": "HIGH"
      }
      // ... additional tasks for Machines factors
    ],
    "Materials": [
      {
        "task_description": "Check raw material quality certificates and supplier audit reports.",
        "evidence_required": [
          "Raw material quality certificates",
          "Supplier audit reports",
          "Material specification documents from the master data"
        ],
        "instructions": "Compare the raw material quality data of the current batch with the master product specifications. Identify any inconsistencies or quality issues.",
        "rationale": "Variations in material quality can affect the final product quality and may be a root cause.",
        "initial_confidence": "MEDIUM"
      }
      // ... additional tasks for Materials factors
    ],
    "Measurements": [
      {
        "task_description": "Review instrument calibration records and sampling protocols.",
        "evidence_required": [
          "Calibration records for measurement instruments",
          "Sampling procedure documents",
          "Test method documentation"
        ],
        "instructions": "Ensure that the measurement instruments are properly calibrated and that the sampling protocols align with the master documentation.",
        "rationale": "Incorrect measurements can falsely indicate deviations; verifying instrument accuracy is essential.",
        "initial_confidence": "HIGH"
      }
      // ... additional tasks for Measurements factors
    ],
    "Environment": [
      {
        "task_description": "Examine environmental monitoring records for the production area.",
        "evidence_required": [
          "Environmental monitoring logs",
          "Temperature and humidity records",
          "Maintenance records for environmental control systems"
        ],
        "instructions": "Compare the recorded environmental conditions during production against the master environmental requirements. Look for any deviations (e.g., temperature, humidity) that could have influenced the process.",
        "rationale": "Environmental conditions can impact process performance, so deviations here may contribute to the quality event.",
        "initial_confidence": "MEDIUM"
      }
      // ... additional tasks for Environment factors
    ]
  }
}

Notes:
Generate tasks for each factor identified in the Ishikawa factors JSON.
Ensure that each task clearly ties back to the evidence required and the factor's rationale.
Use the unified structure and maintain clarity and specificity in each task."

Unified JSON Template (with Inline Comments):
{
  "investigative_tasks": {
    "People": [
      {
        "task_description": "", // e.g., "Review operator training records and shift logs."
        "evidence_required": [
          // List the documents or data sources, e.g., "Operator training records", "Shift attendance logs"
        ],
        "instructions": "", // Detailed steps on what to verify or compare.
        "rationale": "",    // Why this task is relevant (e.g., "Operator error could have contributed to the deviation.")
        "initial_confidence": "" // HIGH, MEDIUM, or LOW based on the factor's confidence.
      }
      // ... additional tasks for People factors
    ],
    "Methods": [
      {
        "task_description": "",
        "evidence_required": [
          // e.g., "SOP documents", "Process execution logs"
        ],
        "instructions": "",
        "rationale": "",
        "initial_confidence": ""
      }
      // ... additional tasks for Methods factors
    ],
    "Machines": [
      {
        "task_description": "",
        "evidence_required": [
          // e.g., "Calibration logs", "Maintenance records"
        ],
        "instructions": "",
        "rationale": "",
        "initial_confidence": ""
      }
      // ... additional tasks for Machines factors
    ],
    "Materials": [
      {
        "task_description": "",
        "evidence_required": [
          // e.g., "Raw material quality certificates", "Supplier audit reports"
        ],
        "instructions": "",
        "rationale": "",
        "initial_confidence": ""
      }
      // ... additional tasks for Materials factors
    ],
    "Measurements": [
      {
        "task_description": "",
        "evidence_required": [
          // e.g., "Instrument calibration records", "Sampling protocol documents"
        ],
        "instructions": "",
        "rationale": "",
        "initial_confidence": ""
      }
      // ... additional tasks for Measurements factors
    ],
    "Environment": [
      {
        "task_description": "",
        "evidence_required": [
          // e.g., "Environmental monitoring logs", "Temperature/humidity records"
        ],
        "instructions": "",
        "rationale": "",
        "initial_confidence": ""
      }
      // ... additional tasks for Environment factors
    ]
  }
}


Explanation:
Unified Structure:
The template uses the same keys for every category, ensuring consistency across all tasks.
Inline Comments:
The JSONC-like inline comments explain what each key should contain, guiding the LLM to extract the necessary information without preset biases.
Specificity and Actionability:
Each task prompt is designed to be specific and actionable, directing investigators exactly on what evidence to gather, how to verify it, and why it matters.
Confidence Assignment:
Each task is also assigned an initial confidence level based on the underlying factor's support from the previous identification step.





