package server;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ct414.*;

public class AssessmentRegistry {

	private String dbName;
	private HashMap<String, MultipleChoiceAssessment> registeredAssessments;
	
	
	public AssessmentRegistry(String db) {
		this.dbName = db;
		this.registeredAssessments = new HashMap<String, MultipleChoiceAssessment>();
		this.loadRegistry();
	}
	
	private void loadRegistry() {
		ArrayList<String> lines = Utils.loadLines(dbName);
		lines.forEach(line -> {
			int questionCounter = 0;
			String[] assessment = line.split(";");
			String ID = assessment[0];
			String module = assessment[1];
			String info = assessment[2];
			LocalDateTime startDate = LocalDateTime.parse(assessment[3], Utils.formatter);
			LocalDateTime endDate = LocalDateTime.parse(assessment[4], Utils.formatter);
			String[] questionList = assessment[5].split("/");
			HashMap<Integer, MultipleChoiceQuestion> questions = new HashMap<Integer, MultipleChoiceQuestion>();
			for(int i = 0; i < questionList.length; i++) {
				String[] q = questionList[i].split("&");
				String q_detail = q[0];
				String[] options = {q[1], q[2], q[3], q[4]};
				int ans = Integer.parseInt(q[5]);
				MultipleChoiceQuestion question = new MultipleChoiceQuestion(questionCounter, q_detail, options, ans);
				questions.put(questionCounter, question);
				questionCounter++;
			}
			registeredAssessments.put(ID, new MultipleChoiceAssessment(ID, module, info, startDate, endDate, questions));
		});
	}
	
	public MultipleChoiceAssessment getAssessmentByID(String id) throws NoMatchingAssessment {
		MultipleChoiceAssessment assessment = this.registeredAssessments.get(id);
		if (assessment == null) {
			throw new NoMatchingAssessment("Assessment Not Found");
		}
		if (assessment.getClosingDate().isBefore(LocalDateTime.now())) {
			throw new NoMatchingAssessment("Assessment Has Expired");
		}
		return assessment;
	}
	
	public ArrayList<MultipleChoiceAssessment> getAssessmentsForModules(String[] modules) throws NoMatchingAssessment{
		ArrayList<MultipleChoiceAssessment> assessments = new ArrayList<MultipleChoiceAssessment>();
		for(String module : modules) {
			ArrayList<MultipleChoiceAssessment> mcqs = this.getAssessmentsForModule(module);
			for(MultipleChoiceAssessment mcq: mcqs) {
				assessments.add(mcq);
			}
		}
		if (assessments.isEmpty()) {
			throw new NoMatchingAssessment("");
		}
		return assessments;
	}
	
	private ArrayList<MultipleChoiceAssessment> getAssessmentsForModule(String module) {
		ArrayList<MultipleChoiceAssessment> assessments = new ArrayList<MultipleChoiceAssessment>();
		this.registeredAssessments.values().forEach(assessment -> {
			if(assessment.getModule().equals(module)) {
				assessments.add(assessment);
			}
		});
		return assessments;
		
	}

	public MultipleChoiceAssessment markAssessment(MultipleChoiceAssessment assessment) {
		Integer denominator = assessment.getQuestions().size();
		Integer numerator = 0;
		ArrayList<MultipleChoiceQuestion> questions = new ArrayList<MultipleChoiceQuestion>();
		for(Question q: assessment.getQuestions()){
			questions.add((MultipleChoiceQuestion) q);
		}
		for (MultipleChoiceQuestion question : questions) {
			if (question.getSelectedAnswer() == question.getCorrectAnswer()){
				numerator++;
			}
		}
		Double marks = ((numerator * 1.0)  / (denominator * 1.0)) * 100.00;
		assessment.setMarks(marks.toString());
		return assessment;
	}
}
