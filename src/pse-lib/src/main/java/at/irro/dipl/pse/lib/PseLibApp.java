package at.ac.tuwien.ifs.semanticweb;

import org.apache.jena.graph.Node;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.*;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.System.out;

public class TulidApp {

    private boolean shutdown = false;

    private Map<String, String> prefixes;
    private String rdfPrefix;
    private String teachPrefix;
    private String foafPrefix;
    private String aiisoPrefix;
    private String tulidPrefix;
    private String dcPrefix;
    private String jusoPrefix;
    private String crswPrefix;
    private String doapPrefix;


    private Property rdfTypeProperty;

    private Property dcIdenifierProperty;
    private Property dcTitleDeProperty;
    private Property dcTitleProperty;
    ;
    private Property aiisoCodeProperty;
    private Property aiisoPartOfProperty;

    private Property jusoLocatorAddressProperty;
    private Property jusoFullAddressProperty;

    private Property foafFamilyNameProperty;
    private Property foafFirstNameProperty;
    private Property foafNameProperty;
    private Property foafPhoneProperty;
    private Property foafMboxProperty;
    private Property foafGenderProperty;

    private Property teachTeacherProperty;
    private Property teachCourseTitleProperty;
    private Property teachBookingNumberProperty;
    private Property teachWeeklyHoursProperty;
    private Property teachEctsProperty;
    private Property teachAcademicTermProperty;

    private Property crswHasStudentInteractionTypeProperty;

    private Property doapNameProperty;
    private Property doapCreatedProperty;

    private Property tulidDoneByProperty;
    private Property tulidMemberOfProperty;
    private Property tulidAdministrationProperty;
    private Property tulidHasLocationProperty;
    private Property tulidEndDateProperty;
    private Property tulidFunctionProperty;
    private Property tulidFunctionCategoryProperty;
    private Property tulidHasOfficeProperty;
    private Property tulidFaxNumberProperty;
    private Property tulidAddressProperty;
    private Property tulidBuildingProperty;

    private Resource teacherResource;
    private Resource organisationResource;
    private Resource facultyResource;
    private Resource instituteResource;
    private Resource departmentResource;
    private Resource courseResource;
    private Resource personResource;
    private Resource projectResource;
    private Resource addressResource;

    private OntModel ontModel;
    private Model model;

    private Map<String,String> usageMap = new HashMap();

    public static void main(String[] args) throws IOException {

        TulidApp app = new TulidApp();
        app.repl();

    }

    public TulidApp() throws FileNotFoundException {


        model = ModelFactory.createDefaultModel();
        model.read(new FileInputStream("src/main/resources/courses.ttl"), null, "TTL");
        model.read(new FileInputStream("src/main/resources/departments.ttl"), null, "TTL");
        model.read(new FileInputStream("src/main/resources/institutes.ttl"), null, "TTL");
        model.read(new FileInputStream("src/main/resources/faculty.ttl"), null, "TTL");
        model.read(new FileInputStream("src/main/resources/persons.ttl"), null, "TTL");
        model.read(new FileInputStream("src/main/resources/projects.ttl"), null, "TTL");
        model.read(new FileInputStream("src/main/resources/rooms.ttl"), null, "TTL");

        InputStream in = new FileInputStream("src/main/resources/tulid.owl");
        RDFDataMgr.read(model, in, Lang.TURTLE);
        ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RDFS_INF, model);

        //map for all used prefixes in the ttl file
        prefixes = model.getNsPrefixMap();
        rdfPrefix = prefixes.get("rdf");
        teachPrefix = prefixes.get("teach");
        foafPrefix = prefixes.get("foaf");
        aiisoPrefix = prefixes.get("aiiso");
        tulidPrefix = prefixes.get("tulid");
        dcPrefix = prefixes.get("dc");
        jusoPrefix = prefixes.get("juso");
        crswPrefix = prefixes.get("crsw");
        doapPrefix = prefixes.get("doap");

        // properties
        rdfTypeProperty = model.getProperty(rdfPrefix + "type");

        aiisoCodeProperty = model.getProperty(aiisoPrefix + "code");
        aiisoPartOfProperty = model.getProperty(aiisoPrefix + "part_of");

        tulidDoneByProperty = model.getProperty(tulidPrefix + "doneBy");
        tulidHasLocationProperty = model.getProperty(tulidPrefix + "hasLocation");
        tulidAdministrationProperty = model.getProperty(tulidPrefix + "administration");
        tulidMemberOfProperty = model.getProperty(tulidPrefix + "memberOf");
        tulidEndDateProperty = model.getProperty(tulidPrefix + "endDate");
        tulidFunctionProperty = model.getProperty(tulidPrefix + "function");
        tulidFunctionCategoryProperty = model.getProperty(tulidPrefix + "functionCategory");
        tulidHasOfficeProperty = model.getProperty(tulidPrefix + "hasOffice");
        tulidFaxNumberProperty = model.getProperty(tulidPrefix + "faxNumber");
        tulidAddressProperty = model.getProperty(tulidPrefix + "address");
        tulidBuildingProperty = model.getProperty(tulidPrefix + "building");

        dcTitleProperty = model.getProperty(dcPrefix + "title");
        dcTitleDeProperty = model.getProperty(dcPrefix + "title@de");
        dcIdenifierProperty = model.getProperty(dcPrefix + "identifier");

        teachTeacherProperty = model.getProperty(teachPrefix + "teacher");
        teachBookingNumberProperty = model.getProperty(teachPrefix + "bookingNumber");
        teachWeeklyHoursProperty = model.getProperty(teachPrefix + "weeklyHours");
        teachEctsProperty = model.getProperty(teachPrefix + "ects");
        teachCourseTitleProperty = model.getProperty(teachPrefix + "courseTitle");
        teachAcademicTermProperty = model.getProperty(teachPrefix + "academicTerm");

        jusoLocatorAddressProperty = model.getProperty(jusoPrefix + "locator_address");
        jusoFullAddressProperty = model.getProperty(jusoPrefix + "full_address");

        foafFamilyNameProperty = model.getProperty(foafPrefix + "familyName");
        foafFirstNameProperty = model.getProperty(foafPrefix + "firstName");
        foafNameProperty = model.getProperty(foafPrefix + "name");
        foafGenderProperty = model.getProperty(foafPrefix + "gender");
        foafPhoneProperty = model.getProperty(foafPrefix + "phone");
        foafMboxProperty = model.getProperty(foafPrefix + "mbox");

        doapNameProperty = model.getProperty(doapPrefix + "name");
        doapCreatedProperty = model.getProperty(doapPrefix + "created");

        crswHasStudentInteractionTypeProperty = model.getProperty(crswPrefix + "has-student-interaction-type");

        // resources, we need them to use as objects when checking for "a class" relations
        teacherResource = model.getResource(teachPrefix + "Teacher");
        organisationResource = model.getResource(foafPrefix + "Organisation");
        courseResource = model.getResource(aiisoPrefix + "Course");
        facultyResource = model.getResource(aiisoPrefix + "Faculty");
        instituteResource = model.getResource(aiisoPrefix + "Institute");
        departmentResource = model.getResource(aiisoPrefix + "Department");
        personResource = model.getResource(foafPrefix + "Person");
        projectResource = model.getResource(doapPrefix + "Project");
        addressResource = model.getResource(jusoPrefix + "Address");


        // save the usages, for easy recall
        usageMap.put("all-persons",                 "");
        usageMap.put("all-teachers",                "");
        usageMap.put("all-organisations",           "");
        usageMap.put("all-faculties",               "");
        usageMap.put("all-institutes",              "");
        usageMap.put("all-departments",             "");
        usageMap.put("all-projects",                "");
        usageMap.put("all-addresses",               "");
        usageMap.put("all-courses",                 "");
        usageMap.put("get-institute",               "<inst-number>");
        usageMap.put("institute-all-courses",       "<inst-number>");
        usageMap.put("institute-all-persons",       "<inst-number>");
        usageMap.put("institute-all-rooms",         "<inst-number>");
        usageMap.put("institute-related-orgunits",  "<inst-number>");
        usageMap.put("institute-all-projects",      "<inst-number>");
        usageMap.put("person-courses",              "<firstname> <lastname>");
        usageMap.put("quit, q, exit",               "");
    }

    /**
     * the REPL (read-evaluate-print-loop) powering this tool
     */
    public void repl() throws IOException {

        out.println();
        out.println();
        out.println("-------------------------------------------------------------------------------");
        out.println("> Hello Semantic Web!");

        while (!shutdown) {
            out.print("> ");
            final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            final String line = reader.readLine().trim();
            final String[] commands = line.split(" ");
            final String cmd = commands[0]; // 'cause we are lazy

            if (isCmd(cmd,"")) {
                // only Enter/Carriage Return was pressed, simply continue the REPL
                continue;
            } else if (isCmd(cmd,"quit","q","exit")) {
                shutdown = true;
            } else if (isCmd(cmd,"help")) {
                printHelp();
            } else if (isCmd(cmd,"hi","Hi")) {
                out.println("Hey!");
            } else if (isCmd(cmd,"all-persons")){
                printAllPersons();
            } else if (isCmd(cmd,"all-teachers")){
                printAllTeachers();
            } else if (isCmd(cmd,"all-organisations")){
                printAllOrganisations();
            } else if (isCmd(cmd,"all-faculties")){
                printAllFaculties();
            } else if (isCmd(cmd,"all-institutes")){
                printAllInstitutes();
            } else if (isCmd(cmd,"all-departments")){
                printAllDepartments();
            } else if (isCmd(cmd,"all-projects")){
                printAllProjects();
            } else if (isCmd(cmd,"all-addresses")){
                printAllAddresses();
            } else if (isCmd(cmd,"all-courses")) {
                printAllCourses();
            } else if (isCmd(cmd,"get-institute")){
                if (commands.length == 2) {
                    printInstitute(commands[1]);
                } else {
                    printUsage(cmd);
                }
            } else if (isCmd(cmd,"institute-all-courses")) {
                if (commands.length == 2) {
                    printCoursesOfInstitute(commands[1]);
                } else {
                    printUsage(cmd);
                }
            } else if (isCmd(cmd,"institute-all-persons")) {
                if (commands.length == 2) {
                    printPersonsByInstitute(commands[1]);
                } else {
                    printUsage(cmd);
                }
            } else if (isCmd(cmd,"institute-all-rooms")) {
                if (commands.length == 2) {
                    printRoomsByInstitute(commands[1]);
                } else {
                    printUsage(cmd);
                }
            } else if (isCmd(cmd,"institute-related-orgunits")) {
                if (commands.length == 2) {
                    printRelatedOrgUnitOfInstitute(commands[1]);
                } else {
                    printUsage(cmd);
                }
            } else if (isCmd(cmd,"institute-all-projects")) {
                if (commands.length == 2) {
                    printProjectsByInstitute(commands[1]);
                } else {
                    printUsage(cmd);
                }
            } else if (isCmd(cmd,"person-courses")) {
                if (commands.length == 3) {
                    printCoursesOfTeacher(commands[1]+" "+commands[2]);
                } else {
                    printUsage(cmd);
                }
            } else {
                out.println("Unknown command '"+cmd+"'. Type 'help' for all commands");
            }
        }


        out.println("Bye Semantic Web!");
        out.println("-------------------------------------------------------------------------------");
    }

/* * * * * * * * * * * * * * * * * * * * * * * * * UTILS * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    private boolean isCmd(String input, String cmd){
        return input.equals(cmd);
    }

    private boolean isCmd(String input, String... cmds){
        for(String cmd : cmds){
            if(isCmd(input,cmd)){
                return true;
            }
        }
        return false;
    }

    private void printUsage(String cmd){
        if(usageMap.containsKey(cmd)){
            String args = usageMap.get(cmd);
            out.println("Command parsing error");
            out.println("Usage: "+cmd+" "+args);
        } else {
            out.println("No usage for '"+cmd+"' found");
        }
    }

    private void printHelp(){
        out.println("This is an interactive REPL to explore the semantic dataset of TU linked data");
        out.println("The following commands are available:");
        out.println();
        for( String key : usageMap.keySet().stream().sorted().collect(Collectors.toList())){
            out.println(key+" "+usageMap.get(key));
        }
        out.println();
        out.println("Feel free to play around!");
        out.println();
    }

/* * * * * * * * * * * * * * * * * * * * * LEXICAL FORMS OF RESOURCES * * * * * * * * * * * * * * * * * * * * * * * * */

    private String getLexicalFormOfLiteral(Resource resource, Property property){
        String lexicalForm = "";
        if(resource.hasProperty(property)){
            RDFNode node = resource.getProperty(property).getObject();
            if(node.isLiteral() && node.asLiteral() != null){
                lexicalForm = node.asLiteral().getLexicalForm();
            }
        }
        return lexicalForm;
    }

    private String getShortLexicalFormOfPerson(Resource person){
        String firstName = getLexicalFormOfLiteral(person,foafFirstNameProperty);
        String lastName = getLexicalFormOfLiteral(person,foafFamilyNameProperty);
        String oid = getLexicalFormOfLiteral(person,dcIdenifierProperty);
        String lexicalForm = "";
        if(!firstName.equals("")){
            lexicalForm += firstName;
        }
        if(!lastName.equals("")){
            lexicalForm += " "+lastName;
        }
        if(!oid.equals("")){
            lexicalForm += " ("+oid+")";
        }
        return lexicalForm;
    }

    private String getShortLexicalFormOfCourse(Resource course){
        String title = getLexicalFormOfLiteral(course,teachCourseTitleProperty);
        String term = getLexicalFormOfLiteral(course,teachAcademicTermProperty);
        String interActionType = null;
        if(course.hasProperty(crswHasStudentInteractionTypeProperty)){
            RDFNode node = course.getProperty(crswHasStudentInteractionTypeProperty).getObject();
            if(node.isResource() && node.asResource() != null){
                Resource studentInteractionTypeResource = node.asResource();
                if(studentInteractionTypeResource.hasProperty(dcTitleProperty)){
                    interActionType = studentInteractionTypeResource.getProperty(dcTitleProperty).getObject().asLiteral().getLexicalForm();
                }
            }
        }
        String lexicalForm = "";
        if(interActionType != null && !interActionType.equals("")){
            lexicalForm += interActionType + " ";
        }
        if(!title.equals("")){
            lexicalForm += title;
        }
        if(!term.equals("")){
            lexicalForm += ", "+term;
        }
        return lexicalForm;
    }

    private String getShortLexicalFormOfOrganisation(Resource organisation){
        String number = getLexicalFormOfLiteral(organisation,aiisoCodeProperty);
        String title = getLexicalFormOfLiteral(organisation,dcTitleProperty);
        String lexicalForm = null;
        if(!number.equals("")){
            lexicalForm = number;
            if(!title.equals("")){
                lexicalForm += " "+title;
            }
        }
        return lexicalForm;
    }

    private String getShortLexicalFormOfAddress(Resource address){
        String code = getLexicalFormOfLiteral(address,jusoLocatorAddressProperty);
        String building = getLexicalFormOfLiteral(address,tulidBuildingProperty);
        String lexicalForm = null;
        if(!code.equals("")){
            lexicalForm = code;
            if(!building.equals("")){
                lexicalForm += " ("+building+")";
            }
        }
        return lexicalForm;
    }

/* * * * * * * * * * * * * * * * * * * * * * PRINT SINGLE RESOURCES * * * * * * * * * * * * * * * * * * * * * * * * * */

    private void printURI(Resource r){
        out.println("URI: "+ (r.getURI()==null ? "(bank-node)" : r.getURI()) );
    }

    private void printPerson(Resource person){
        String name = getLexicalFormOfLiteral(person,foafFirstNameProperty) + " " + getLexicalFormOfLiteral(person,foafFamilyNameProperty);
        String oid = getLexicalFormOfLiteral(person,dcIdenifierProperty);
        if(!name.equals("")) {
            if(!oid.equals("")){
                out.println(name+" ("+oid+")");
            } else {
                out.println(name);
            }
        } else {
            if(!oid.equals("")){
                out.println("OID: "+oid);
            } else {
                out.println("<unknown>");
            }
        }
        String function = getLexicalFormOfLiteral(person,tulidFunctionProperty);
        if(!function.equals(""))
            out.println(function);
        String mbox = getLexicalFormOfLiteral(person,foafMboxProperty);
        if(!mbox.equals(""))
            out.println("M: "+mbox);
        String phone = getLexicalFormOfLiteral(person,foafPhoneProperty);
        if(!phone.equals(""))
            out.println("T: "+phone);
        if(person.hasProperty(tulidMemberOfProperty)) {
            List<Resource> organisations = getOrganisationsOfPerson(person);
            if(!organisations.isEmpty()){
                out.println("Member of:");
                organisations.stream()
                        .forEach(o -> out.println("  * " + getShortLexicalFormOfOrganisation(o)));
            }
        }

        if(person.hasProperty(tulidHasOfficeProperty)) {
            RDFNode node = person.getProperty(tulidHasOfficeProperty).getObject();
            if(node.isResource() && node.asResource() != null){
                Resource address = node.asResource();
                if(address.hasProperty(jusoLocatorAddressProperty)){
                    out.print("Office: " + getShortLexicalFormOfAddress(address)+"\n");
                }
            }
        }

        // check if this is a teacher
        List<Resource> courses = getCoursesOfTeacher(person);
        if(!courses.isEmpty()){
            out.println("Courses:");
            courses.stream()
                    .forEach(c -> out.println("  * " + getShortLexicalFormOfCourse(c)));
        }

        printURI(person);
    }

    private void printOrganisation(Resource organisation){
        String number = getLexicalFormOfLiteral(organisation,aiisoCodeProperty);
        String title = getLexicalFormOfLiteral(organisation,dcTitleProperty);
        if(!number.equals("")){
            String line = number;
            if(!title.equals("")){
                line += " "+title;
            }
            out.println(line);
        }
        String mbox = getLexicalFormOfLiteral(organisation,foafMboxProperty);
        if(!mbox.equals(""))
            out.println("M: "+mbox);
        String phone = getLexicalFormOfLiteral(organisation,foafPhoneProperty);
        if(!phone.equals(""))
            out.println("T: "+phone);
        String fax = getLexicalFormOfLiteral(organisation,tulidFaxNumberProperty);
        if(!fax.equals(""))
            out.println("F: "+fax);
        if(organisation.hasProperty(tulidAddressProperty)) {
            RDFNode node = organisation.getProperty(tulidAddressProperty).getObject();
            if(node.isResource() && node.asResource() != null){
                Resource addressResource = node.asResource();
                if(addressResource.hasProperty(jusoFullAddressProperty)){
                    out.println("Address: " + addressResource.getProperty(jusoFullAddressProperty).getObject().asLiteral().getLexicalForm());
                }
            }
        }
        if(organisation.hasProperty(aiisoPartOfProperty)){
            RDFNode node = organisation.getProperty(aiisoPartOfProperty).getObject();
            if(node.isResource() && node.asResource() != null){
                Resource partOfOrgUnitResource = node.asResource();
                out.print("Part of: ");
                number = getLexicalFormOfLiteral(partOfOrgUnitResource,aiisoCodeProperty);
                title = getLexicalFormOfLiteral(partOfOrgUnitResource,dcTitleProperty);
                if(!number.equals("")){
                    String line = number;
                    if(!title.equals("")){
                        line += " "+title;
                    }
                    out.println(line);
                }
            }
        }

        printURI(organisation);
    }

    private void printAddress(Resource address){
        String code = getLexicalFormOfLiteral(address,jusoLocatorAddressProperty);
        String building = getLexicalFormOfLiteral(address,tulidBuildingProperty);
        String fullAddress = getLexicalFormOfLiteral(address,jusoFullAddressProperty);
        if(!code.equals("")){
            String line = code;
            if(!building.equals("")){
                line += " ("+building+")";
            }
            out.println(line);
        }
        if(!fullAddress.equals("")){
            out.println(fullAddress);
        }

        printURI(address);
    }

    private void printProject(Resource project){
        String name = getLexicalFormOfLiteral(project, doapNameProperty);
        if(!name.equals("")){
            out.println(name);
        }
        String createdAt = getLexicalFormOfLiteral(project, doapCreatedProperty);
        String endAt = getLexicalFormOfLiteral(project, tulidEndDateProperty);
        if(!createdAt.equals("")){
            String line = createdAt;
            if(!endAt.equals("")){
                line += " - "+endAt;
            }
            out.println(line);
        }
        if(project.hasProperty(tulidDoneByProperty)){
            Resource institute = project.getProperty(tulidDoneByProperty).getObject().asResource();
            out.print("Done by: " + getShortLexicalFormOfOrganisation(institute)+"\n");
        }

        printURI(project);
    }

    private void printCourse(Resource course){

        String title = getLexicalFormOfLiteral(course,teachCourseTitleProperty);
        String term = getLexicalFormOfLiteral(course,teachAcademicTermProperty);
        String interActionType = null;
        if(course.hasProperty(crswHasStudentInteractionTypeProperty)){
            RDFNode node = course.getProperty(crswHasStudentInteractionTypeProperty).getObject();
            if(node.isResource() && node.asResource() != null){
                Resource studentInteractionTypeResource = node.asResource();
                if(studentInteractionTypeResource.hasProperty(dcTitleProperty)){
                    interActionType = studentInteractionTypeResource.getProperty(dcTitleProperty).getObject().asLiteral().getLexicalForm();
                }
            }
        }
        String line = "";
        if(interActionType != null && !interActionType.equals("")){
            line += interActionType + " ";
        }
        if(!title.equals("")){
            line += title;
        }
        if(!term.equals("")){
            line += ", "+term;
        }
        out.println(line);

        String bookingNumber = getLexicalFormOfLiteral(course,teachBookingNumberProperty);
        String weeklyHours = getLexicalFormOfLiteral(course,teachWeeklyHoursProperty);
        String ects = getLexicalFormOfLiteral(course,teachEctsProperty);
        line = "";
        if(!bookingNumber.equals("")){
            line += bookingNumber;
        }
        if(!ects.equals("")){
            line += ", "+ects+" ECTS";
        }
        if(!weeklyHours.equals("")){
            line += ", "+weeklyHours+" SWS";
        }
        out.println(line);

        String teacher = "";
        if(course.hasProperty(teachTeacherProperty)){
            RDFNode node = course.getProperty(teachTeacherProperty).getObject();
            if(node.isResource() && node.asResource() != null){
                Resource teacherResource = node.asResource();
                teacher = getShortLexicalFormOfPerson(teacherResource);
            }
        }
        if(!teacher.equals("")){
            out.println("Lecturer: " + teacher);
        }

        printURI(course);
    }

/* * * * * * * * * * * * * * * * * * * * * * * PRINT LIST RESOURCES * * * * * * * * * * * * * * * * * * * * * * * * * */

    private void printAllPersons() {
        ResIterator iter = ontModel.listSubjectsWithProperty(rdfTypeProperty,personResource);
        while(iter.hasNext()){
            Resource person = iter.next();
            printPerson(person);
            out.println();
        }
    }

    private void printAllTeachers() {
        NodeIterator iter = ontModel.listObjectsOfProperty(teachTeacherProperty);
        while (iter.hasNext()) {
            RDFNode node = iter.next();
            if (node.isResource() && node.asResource() != null) {
                Resource r = node.asResource();
                printPerson(r);
                out.println();
            }
        }
    }

    private void printAllOrganisations() {
        ResIterator iter = ontModel.listSubjectsWithProperty(rdfTypeProperty,organisationResource);
        while(iter.hasNext()){
            Resource organisation = iter.next();
            printOrganisation(organisation);
            out.println();
        }
    }

    private void printAllFaculties() {
        ResIterator iter = ontModel.listSubjectsWithProperty(rdfTypeProperty,facultyResource);
        while(iter.hasNext()){
            Resource faculty = iter.next();
            if(faculty.getProperty(rdfTypeProperty).getObject().asResource().getURI().equals(facultyResource.getURI())){
                printOrganisation(faculty);
                out.println();
            }
        }
    }

    private void printInstitute(String instCode){
        Resource institute = getInstituteByCode(instCode);
        if(institute != null){
            printOrganisation(institute);
        } else {
            out.println();
            out.println("No institute found for code '"+instCode+"'");
            out.println();
        }
    }

    private void printAllInstitutes() {
        ResIterator iter = ontModel.listSubjectsWithProperty(rdfTypeProperty,instituteResource);
        while(iter.hasNext()){
            Resource institute = iter.next();
            if(institute.getProperty(rdfTypeProperty).getObject().asResource().getURI().equals(instituteResource.getURI())){
                printOrganisation(institute);
                out.println();
            }
        }
    }

    private void printAllDepartments() {
        ResIterator iter = ontModel.listSubjectsWithProperty(rdfTypeProperty,departmentResource);
        while(iter.hasNext()){
            Resource department = iter.next();
            if(department.getProperty(rdfTypeProperty).getObject().asResource().getURI().equals(departmentResource.getURI())){
                printOrganisation(department);
                out.println();
            }
        }
    }

    private void printAllProjects(){
        ResIterator iter = ontModel.listSubjectsWithProperty(rdfTypeProperty,projectResource);
        while(iter.hasNext()){
            Resource project = iter.next();
            printProject(project);
            out.println();
        }
    }

    private void printAllAddresses(){
        ResIterator iter = ontModel.listSubjectsWithProperty(rdfTypeProperty,addressResource);
        while(iter.hasNext()){
            Resource address = iter.next();
            printAddress(address);
            out.println();
        }
    }

    private void printAllCourses(){
        ResIterator iter = ontModel.listSubjectsWithProperty(rdfTypeProperty,courseResource);
        while(iter.hasNext()){
            Resource course = iter.next();
            printCourse(course);
            out.println();
        }
    }

/* * * * * * * * * * * * * * * * * * * * * * GETTER FOR RELATION-OBJECTS * * * * * * * * * * * * * * * * * * * * * * * * */

    private List<Resource> getCoursesOfTeacher(Resource teacher){
        List<Resource> courses = new LinkedList<>();
        ResIterator coursesIter = ontModel.listSubjectsWithProperty(teachTeacherProperty, teacher);
        coursesIter.forEachRemaining(c -> courses.add(c));
        return courses;
    }

    private List<Resource> getAdministeredRoomsOfInstitute(Resource institute){
        List<Resource> rooms = new LinkedList<>();
        ResIterator roomsIter = ontModel.listSubjectsWithProperty(tulidAdministrationProperty,institute);
        roomsIter.forEachRemaining(r -> rooms.add(r));
        return rooms;
    }

    private Resource getInstituteByCode(String code){
        // TODO this commented code below produced wrong result. not sure why though
//        ResIterator iter = ontModel.listSubjectsWithProperty(rdfTypeProperty,instituteResource); // this should be all institutes
//        if(iter.hasNext()) {
//            Resource institute = iter.next();
//            if(institute.hasProperty(aiisoCodeProperty)){
//                RDFNode node = institute.getProperty(aiisoCodeProperty).getObject();
//                if(node.isLiteral() && node.asLiteral().getLexicalForm().equals(code)){
//                    return institute;
//                }
//            }
//            return iter.next();
//        }
//        return null;

        ResIterator iter = ontModel.listSubjectsWithProperty(aiisoCodeProperty,code);
        if(iter.hasNext()){
            Resource institute = iter.next();
            if(institute.hasProperty(rdfTypeProperty,instituteResource)){
                return institute;
            }
        }
        return null;
    }

    private List<Resource> getOrganisationsOfPerson(Resource person){

        NodeIterator iter = ontModel.listObjectsOfProperty(person,tulidMemberOfProperty);

        List<Resource> organisations = new LinkedList<>();
        while(iter.hasNext()){
            RDFNode node = iter.next();
            if(node.isResource() && node.asResource() != null){
                organisations.add(node.asResource());
            }
        }
        return organisations;
    }


    private void printRelatedOrgUnitOfInstitute(String instCode) {
        Resource institute = getInstituteByCode(instCode);
        String instituteName = getShortLexicalFormOfOrganisation(institute);
        if(instituteName == null || instituteName.equals("")){
            out.println("\nNo Institute found for '"+instCode+"'\n");
            return;
        }

        List<Resource> relatedOrganisations = getRelatedOrganisationsByInstitute(institute);

        out.println();
        if (relatedOrganisations.size() == 0) {
            out.println("No related Organizational Units found for '"+instituteName+"'");
            out.println();
        } else {
            out.println("The following Organizational Units are related to '"+instituteName+"':" + "\n");
            for (Resource organisation : relatedOrganisations) {
                printOrganisation(organisation);
                out.println();
            }
        }
    }


    private void printPersonsByInstitute(String instCode) {
        Resource institute = getInstituteByCode(instCode);
        String instituteName = getShortLexicalFormOfOrganisation(institute);
        if(instituteName == null || instituteName.equals("")){
            out.println("\nNo Institute found for '"+instCode+"'\n");
            return;
        }

        List<Resource> persons = getPersonsByInstitute(institute);

        out.println();
        if (persons.size() == 0) {
            out.println("No Persons found for '"+instituteName+"'");
            out.println();
        } else {
            out.println("The following Persons are members of '"+instituteName+"':" + "\n");
            for (Resource person : persons) {
                printPerson(person);
                out.println();
            }
        }

    }

    private Resource getAddressOfRoom(Resource room){
        NodeIterator roomIter = ontModel.listObjectsOfProperty(room,tulidHasLocationProperty);
        if(roomIter.hasNext()){
            RDFNode node = roomIter.next();
            if(node.isResource() && node.asResource() != null){
                return node.asResource();
            }
        }
        return null;

//        ResIterator roomIter = ontModel.listSubjectsWithProperty(tulidHasLocationProperty,room);
//        if(roomIter.hasNext()){
//            return roomIter.next();
//        }
//        return null;
    }

    private void printProjectsByInstitute(String instCode) {
        Resource institute = getInstituteByCode(instCode);
        String instituteName = getShortLexicalFormOfOrganisation(institute);
        if(instituteName == null || instituteName.equals("")){
            out.println("\nNo Institute found for '"+instCode+"'\n");
            return;
        }

        List<Resource> projects = getProjectsByInstitute(institute);

        out.println();
        if (projects.size() == 0) {
            out.println("No Projects found for '"+instituteName+"'");
            out.println();
        } else {
            out.println("The following Projects are done by '"+instituteName+"':\n");
            for (Resource project : projects) {
                printProject(project);
                out.println();
            }
        }
    }

    private void printRoomsByInstitute(String command) {
        Resource institute = getInstituteByCode(command);
        String instituteName = getShortLexicalFormOfOrganisation(institute);
        if(instituteName == null || instituteName.equals("")){
            out.println("\nNo Institute found for '"+command+"'\n");
            return;
        }

        List<Resource> rooms = getAdministeredRoomsOfInstitute(institute);

        out.println();
        if (rooms.size() == 0) {
            out.println("No Rooms found for '"+instituteName+"'");
            out.println();
        } else {
            out.println("The following Rooms are administrated by '"+instituteName+"':\n");
            for (Resource room : rooms) {
                if(room.hasProperty(dcTitleProperty)){
                    RDFNode node = room.getProperty(dcTitleProperty).getObject();
                    if(node.isLiteral() && node.asLiteral() != null){
                        out.println(node.asLiteral().getLexicalForm());
                    }
                }
                Resource address = getAddressOfRoom(room);
                if(address != null){
                    printAddress(address);
                }
                out.println();
            }
        }
    }

    private List<Resource> getProjectsByInstitute(Resource institute){
        ResIterator iter = ontModel.listSubjectsWithProperty(tulidDoneByProperty,institute);
        List<Resource> projects = new LinkedList<>();
        while (iter.hasNext()) {
            RDFNode node = iter.next();
            if(node.isResource() && node.asResource() != null){
                projects.add(node.asResource());
            }
        }
        return projects;
    }

    private List<Resource> getPersonsByInstitute(Resource institute){
        ResIterator iter = ontModel.listSubjectsWithProperty(tulidMemberOfProperty,institute);

        List<Resource> persons = new LinkedList<>();
        while (iter.hasNext()) {
            RDFNode node = iter.next();
            if(node.isResource() && node.asResource() != null){
                persons.add(node.asResource());
            }
        }
        return persons;
    }

    private List<Resource> getRelatedOrganisationsByInstitute(Resource institute){
        ResIterator iter = ontModel.listSubjectsWithProperty(aiisoPartOfProperty,institute);

        List<Resource> related = new LinkedList<>();
        while (iter.hasNext()) {
            RDFNode node = iter.next();
            if(node.isResource() && node.asResource() != null){
                related.add(node.asResource());
            }
        }
        return related;
    }

    private List<Resource> getCoursesByInstitute(Resource institute){
        List<Resource> courses = new LinkedList<>();
        List<Resource> teachers = getPersonsByInstitute(institute);
        teachers.stream()
                .forEach( t -> courses.addAll(getCoursesOfTeacher(t)) );
        return courses;
    }

    private void printCoursesOfInstitute(String instCode) {
        Resource institute = getInstituteByCode(instCode);
        String instituteName = getShortLexicalFormOfOrganisation(institute);
        if(instituteName == null || instituteName.equals("")){
            out.println("\nNo Institute found for '"+instCode+"'\n");
            return;
        }

        List<Resource> courses = getCoursesByInstitute(institute);

        out.println();
        if(courses.size() == 0){
            out.println("No courses found for '"+instituteName+"'");
            out.println();
        } else {
            out.println("The following Courses are offered by '"+instituteName+"':\n");
            for(Resource course: courses){
                printCourse(course);
                out.println();
            }
        }
    }

    private void printCoursesOfTeacher(String name){
        // there can be multiple pleople who share the same name, so we simply fetch all
        // courses of all people with the same name (the difference will be shown by the
        // OID shown in the courses detailed output )
        ResIterator resIt = ontModel.listSubjectsWithProperty(foafNameProperty, name);
        List<Resource> courses = new LinkedList<>();

        while (resIt.hasNext()){
            Resource personResource = resIt.nextResource();
            if(personResource.hasProperty(rdfTypeProperty, teacherResource)){
                courses.addAll(getCoursesOfTeacher(personResource));
            }
        }

        out.println();
        if(courses.size() == 0){
            out.println("No courses found for '"+name+"'");
            out.println();
        } else {
            out.println("The following Courses are offered by '"+name+"':\n");
            for(Resource course: courses){
                printCourse(course);
                out.println();
            }
        }
    }

}
