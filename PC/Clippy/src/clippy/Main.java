package clippy;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

public class Main extends Application {

    private Stage primaryStage;
    private final Text displayMessageSignup = new Text();
    private final Text displayMessageLogin = new Text();
    private Timer repeatingTask = new Timer();
    final private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddhhmmss", Locale.US);

    private String _urllink;
    private URL _url;
    private String line;
    private String current_user;
    private String prev_pasteData;


    @Override
    public void start(Stage primary_Stage) throws Exception{

        primaryStage = primary_Stage;
        primaryStage.setTitle("C L Y P E");
        primaryStage.setResizable(false);
        primaryStage.initStyle(StageStyle.UNDECORATED);


        Scene signup_scene = getSignUpScene();
        primaryStage.setScene(signup_scene);

        primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("paperclip_1.png")));

        primaryStage.show();
    }

    private Scene getSignUpScene(){
        BorderPane border = new BorderPane();

        HBox hbox = addLeftHbox();
        border.setLeft(hbox);

        GridPane grid = addSignupGridPane();
        border.setCenter(grid);

        HBox topPane = addTopRightHbox();
        border.setTop(topPane);

        HBox bottomPane = addTermsAndConditions();
        border.setBottom(bottomPane);

        Scene newscene = new Scene(border, 620, 570);


        newscene.getStylesheets().add
                (Main.class.getResource("Signup.css").toExternalForm());

        return newscene;
    }

    private Scene getLoginScene(){
        BorderPane border = new BorderPane();

        HBox hbox = addLeftHbox();
        border.setLeft(hbox);

        GridPane grid = addLoginGridPane();
        border.setCenter(grid);

        HBox topPane = addTopRightHbox();
        border.setTop(topPane);

        HBox bottomPane = addTermsAndConditions();
        border.setBottom(bottomPane);

        Scene newscene = new Scene(border, 620, 530);

        newscene.getStylesheets().add
                (Main.class.getResource("Login.css").toExternalForm());

        return newscene;
    }

    private Scene getLoggedInScene(){
        prev_pasteData = "[00000000000000]";
        BorderPane border = new BorderPane();

        GridPane grid = addLoggedinGridPane();
        border.setCenter(grid);

        HBox topPane = addSpecialTopRightHbox();
        border.setTop(topPane);

        Scene newscene = new Scene(border, 620, 280);

        newscene.getStylesheets().add
                (Main.class.getResource("Loggedin.css").toExternalForm());

        return newscene;
    }

    private Stage gettncStage(){
        Stage tnc_stage = new Stage();
        tnc_stage.setTitle("Terms and Conditions");
        tnc_stage.initStyle(StageStyle.UNDECORATED);


        BorderPane borderPane = new BorderPane();

        ScrollPane scrollPane = getTermsAndConditions();
        borderPane.setCenter(scrollPane);

        HBox topPane = addtncTopRightHbox();
        borderPane.setTop(topPane);

        Scene newscene = new Scene(borderPane, 800, 530);

        newscene.getStylesheets().add
                (Main.class.getResource("tnc.css").toExternalForm());

        tnc_stage.setScene(newscene);

        tnc_stage.getIcons().add(new Image(Main.class.getResourceAsStream("paperclip_1.png")));

        return tnc_stage;
    }


    public static void main(String[] args) {
        launch(args);
    }

    private GridPane addLoginGridPane(){
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        addLoginTitle(grid);

        addLoginLables(grid);

        return grid;
    }

    private GridPane addSignupGridPane(){
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        addSignupTitle(grid);

        addSignupLables(grid);

        return grid;
    }

    private GridPane addLoggedinGridPane(){
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        addLoggedInText(grid);



        return grid;
    }

    private void addLoginLables(GridPane grid){
        final PasswordField pwBox = new PasswordField();
        final TextField userTextField = new TextField();

        Label userName = new Label("User Name:");
        grid.add(userName, 2, 4);

        grid.add(userTextField, 3, 4);

        Label pw = new Label("Password:");
        grid.add(pw, 2, 5);

        grid.add(pwBox, 3, 5);

        grid.add(displayMessageLogin, 3, 9);
        displayMessageLogin.setId("action");

        Button btn = new Button("Sign in");
        btn.setId("signin_btn");
        btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                String username = userTextField.getText();
                String password = pwBox.getText();

                if(!accountExists(username, false)){
                    displayMessageLogin.setText("Account not found. Try Again.");
                }
                else if(!verifyAccount(username, password, false)){
                    displayMessageLogin.setText("Invalid Password");
                }
                else{
                    current_user = username;
                    Scene loggedin_scene = getLoggedInScene();
                    primaryStage.setScene(loggedin_scene);
                    startRepeatingTask();
                }


            }
        });
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(btn);
        grid.add(hbBtn, 3, 7);


        Button signup_link = new Button("Don\'t have an account?");
        signup_link.setId("signup_link");
        signup_link.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent e) {
                Scene signup_scene = getSignUpScene();
                primaryStage.setScene(signup_scene);
            }
        });

        HBox hbBtn2 = new HBox(10);
        hbBtn2.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn2.getChildren().add(signup_link);
        grid.add(hbBtn2, 3, 11);
    }


    private void addSignupLables(GridPane grid){
        final PasswordField pwBox = new PasswordField();
        final PasswordField repwBox = new PasswordField();
        final TextField userTextField = new TextField();
        final TextField serial = new TextField();
        final CheckBox checkTnC = new CheckBox();

        Label userName = new Label("User Name:");
        grid.add(userName, 2, 4);

        grid.add(userTextField, 3, 4);

        Label pw = new Label("Password:");
        grid.add(pw, 2, 5);

        grid.add(pwBox, 3, 5);

        Label rpw = new Label("Re-Password:");
        grid.add(rpw, 2, 6);

        grid.add(repwBox, 3, 6);

        Label serial_key = new Label("Serial Key:");
        grid.add(serial_key, 2, 7);

        grid.add(serial, 3, 7);

        // goto row 9
        Label agreeh = new Label("I agree to the Terms and Conditions");
        agreeh.setId("agree");

        HBox hbBtn3 = new HBox(5);
        hbBtn3.setAlignment(Pos.BOTTOM_LEFT);
        hbBtn3.getChildren().addAll(checkTnC, agreeh);
        grid.add(hbBtn3, 2, 9);


        grid.add(displayMessageSignup, 3, 11);
        displayMessageSignup.setId("action");

        Button btn = new Button("Sign up");
        btn.setId("signup_btn");
        btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                String username = userTextField.getText();
                String password = pwBox.getText();
                String rpassword = repwBox.getText();
                String skey = serial.getText();
                boolean cbox = checkTnC.isSelected();


                if(username.isEmpty() || username.length() < 5 || username.length() > 10) {
                    displayMessageSignup.setText("Username between \n  5-15 characters");
                }
                else if(accountExists(username, true)){
                    displayMessageSignup.setText("Account already exists.");
                }
                else if(!validusername(username)){
                    displayMessageSignup.setText("Username cannot have\n special characters");
                }
                else if(password.isEmpty() || password.length() < 5 || password.length() > 15){
                    displayMessageSignup.setText("Password between \n  5-15 characters");

                }
                else if(!password.equals(rpassword)){
                    displayMessageSignup.setText("Password do not match");
                }
                else if(!validserialkey(skey)){
                    displayMessageSignup.setText("Serial key is a \n10 digit number");
                }
                else if(!verifySerial(skey, false)){
                    displayMessageSignup.setText("Invalid Serial key");
                }
                else if(!cbox){
                    displayMessageSignup.setText("You didn\'t agree to\nou T&C");
                }
                else{
                    create_cloudAccount(username + ";" + password);
                    removeSerial(skey);

                    Scene login_scene = getLoginScene();
                    primaryStage.setScene(login_scene);

                    displayMessageLogin.setText("New account created");
                }


            }
        });
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(btn);
        grid.add(hbBtn, 3, 10);

        Button login_link = new Button("Already have an account?");
        login_link.setId("login_link");
        login_link.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent e) {
                Scene login_scene = getLoginScene();
                primaryStage.setScene(login_scene);
            }
        });

        HBox hbBtn2 = new HBox(10);
        hbBtn2.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn2.getChildren().add(login_link);
        grid.add(hbBtn2, 3, 12);



    }

    private void addLoginTitle(GridPane grid){
        Text project = new Text("C L Y P E");
        grid.add(project, 2, 0, 1, 1);
        project.setId("project-text");

        Text scenetitle = new Text("Login");
        grid.add(scenetitle, 2, 1, 1, 1);
        scenetitle.setId("Login-text");
    }

    private void addSignupTitle(GridPane grid){
        Text project = new Text("C L Y P E");
        grid.add(project, 2, 0, 1, 1);
        project.setId("project-text");

        Text scenetitle = new Text("Sign Up");
        grid.add(scenetitle, 2, 1, 1, 1);
        scenetitle.setId("Signup-text");
    }

    private void addLoggedInText(GridPane grid){
        Text scenetitle = new Text("You have logged in successfully.");
        grid.add(scenetitle, 2, 1, 1, 1);
        scenetitle.setId("head-text");

        Text sugg = new Text("You may minimise this window now.");
        grid.add(sugg, 2, 3, 1, 1);
        sugg.setId("sugg-text");
    }

    private HBox addTermsAndConditions(){
        HBox hbox = new HBox(5);
        hbox.setAlignment(Pos.BOTTOM_LEFT);

        Button tnc = new Button("Terms and Conditions");
        tnc.setId("Terms_Conditions");
        tnc.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent e) {
                Stage tncs = gettncStage();
                tncs.show();
            }
        });

        hbox.getChildren().addAll(tnc);
        hbox.setPadding(new Insets(15));

        return hbox;
    }

    private void addLogo(HBox hbox){
        Image logo = new Image(Main.class.getResourceAsStream("paperclip_1.png"));
        ImageView iv1 = new ImageView();
        iv1.setImage(logo);
        iv1.setFitWidth(100);
        iv1.setPreserveRatio(true);
        iv1.setSmooth(true);
        iv1.setCache(true);

        hbox.getChildren().addAll(iv1);
    }

    private void addSeparator(HBox hbox){
        Line line = new Line();
        line.setStartX(0.0f);
        line.setStartY(100.0f);
        line.setEndX(0.0f);
        line.setEndY(-100.0f);
        line.setStroke(Color.color(0.8, 0.2, 0.2));
        line.setStrokeWidth(1);

        hbox.getChildren().add(line);
    }

    private HBox addLeftHbox(){
        HBox leftpane = new HBox();
        leftpane.setAlignment(Pos.CENTER_LEFT);
        leftpane.setPadding(new Insets(25));
        leftpane.setSpacing(20);

        addLogo(leftpane);

        addSeparator(leftpane);

        return leftpane;
    }

    private void addCloseButton(HBox hbox){
        Button close_btn = new Button("x");
        close_btn.setId("x_btn");
        close_btn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                primaryStage.close();
                Platform.exit();
                System.exit(0);

            }
        });
        hbox.getChildren().add(close_btn);
    }

    private void addtncCloseButton(HBox hbox){
        Button close_btn = new Button("x");
        close_btn.setId("x_btn");
        close_btn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                close_btn.getScene().getWindow().hide();
            }
        });
        hbox.getChildren().add(close_btn);
    }

    private void addSpecialCloseButton(HBox hbox){
        Button close_btn = new Button("x");
        close_btn.setId("sx_btn");
        close_btn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                stopRepeatingTask();
                primaryStage.close();
                Platform.exit();
                System.exit(0);

            }
        });
        hbox.getChildren().add(close_btn);
    }

    private void addMinimButton(HBox hbox){
        Button close_btn = new Button("-");
        close_btn.setId("min_btn");
        close_btn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                ((Stage)((Button)e.getSource()).getScene().getWindow()).setIconified(true);

            }
        });
        hbox.getChildren().add(close_btn);
    }

    private void addSpecialMinimButton(HBox hbox){
        Button close_btn = new Button("-");
        close_btn.setId("smin_btn");
        close_btn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                (((Button)e.getSource()).getScene().getWindow()).hide();

            }
        });
        hbox.getChildren().add(close_btn);
    }

    private HBox addTopRightHbox(){
        HBox topPane = new HBox();
        topPane.setAlignment(Pos.TOP_RIGHT);
        topPane.setPadding(new Insets(5));
        topPane.setSpacing(2);

        addMinimButton(topPane);
        addCloseButton(topPane);

        return topPane;
    }

    private HBox addSpecialTopRightHbox(){
        HBox topPane = new HBox();
        topPane.setAlignment(Pos.TOP_RIGHT);
        topPane.setPadding(new Insets(5));
        topPane.setSpacing(2);

        addSpecialMinimButton(topPane);
        addSpecialCloseButton(topPane);

        return topPane;
    }

    private HBox addtncTopRightHbox(){
        HBox topPane = new HBox();
        topPane.setAlignment(Pos.TOP_RIGHT);
        topPane.setPadding(new Insets(5));
        topPane.setSpacing(2);

        addMinimButton(topPane);
        addtncCloseButton(topPane);

        return topPane;
    }

    private String putStamp(String data){
        String stamp = simpleDateFormat.format(new Date());
        return "[" + stamp + "]" + data;
    }




    private String removeStamp(String data){
        return data.substring(16);
    }
    private String getStamp(String data){
        return data.substring(1, 15);
    }
    private int compareStamps(String s1, String s2){
        return s1.compareTo(s2);
    }

    private void startRepeatingTask(){
        repeatingTask.schedule(
                new TimerTask() {
                    String pasteData;
                    String cloudData;

                    @Override
                    public void run() {
                        try{

                            pasteData = getDeviceData();
                            if(pasteData.equals(removeStamp(prev_pasteData))){
                                pasteData = prev_pasteData;
                            }
                            else {
                                pasteData = putStamp(pasteData);
                                prev_pasteData = pasteData;
                            }

                            cloudData = getCloudData();

                            if (!removeStamp(pasteData).equals(removeStamp(cloudData))) {
                                if(compareStamps(getStamp(pasteData), getStamp(cloudData)) > 0)
                                    setCloudData(pasteData);
                                else if(compareStamps(getStamp(pasteData), getStamp(cloudData)) < 0){
                                    prev_pasteData = cloudData;
                                    setDeviceData(removeStamp(cloudData));
                                }
                            }
                        }catch (Exception e){
                            System.out.println("Error in recursive task: " + e.toString());
                        }
                    }
                }, 0, 2000);
    }

    private void stopRepeatingTask(){
        repeatingTask.cancel();
        repeatingTask.purge();
    }

    private boolean validusername(String username){
        return !Pattern.compile("[^A-Za-z0-9]").matcher(username).find();
    }

    private boolean validserialkey(String serialk){
        return (serialk.length() == 10) && !Pattern.compile("[^0-9]").matcher(serialk).find();
    }

    private boolean accountExists(String username, boolean default_val){

        _urllink = "http://dunetest.000webhostapp.com/2346_newCall6764.php?var4=xy";
        line ="";

        try {
            _url = new URL(_urllink);
            HttpURLConnection conn = (HttpURLConnection) _url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            while ((line = in.readLine()) != null){

                if(line.substring(0,line.indexOf(';')).equals(username)) {
                    in.close();
                    return true;
                }
            }

            in.close();
            return false;

        } catch(Exception e) {
            System.out.println("Get Cloud Error: " + e.toString());
        }

        return default_val;
    }

    private boolean verifyAccount(String username, String password, boolean default_val){

        _urllink = "http://dunetest.000webhostapp.com/2346_newCall6764.php?var4=xy";
        line ="";

        try {
            _url = new URL(_urllink);
            HttpURLConnection conn = (HttpURLConnection) _url.openConnection();
            conn.setRequestMethod("GET");


            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            while ((line = in.readLine()) != null){
                if(line.substring(0,line.indexOf(';')).equals(username) && line.substring(line.indexOf(';')+1).equals(password)) {
                    in.close();
                    return true;
                }
            }

            in.close();
            return false;

        } catch(Exception e) {
            System.out.println("Get Cloud Error: " + e.toString());
        }

        return default_val;
    }

    private boolean verifySerial(String serial, boolean default_val){
        _urllink = "http://dunetest.000webhostapp.com/2346_newCall6764.php?var5=xy";
        line ="";

        try {
            _url = new URL(_urllink);
            HttpURLConnection conn = (HttpURLConnection) _url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            while ((line = in.readLine()) != null){
                if(line.equals(serial)) {
                    in.close();
                    return true;
                }
            }

            in.close();
            return false;

        } catch(Exception e) {
            System.out.println("Get Cloud Error: " + e.toString());
        }

        return default_val;
    }

    private void removeSerial(String serialkey) {
        String serials = "";
        line ="";
        try {
            
            _urllink = "http://dunetest.000webhostapp.com/2346_newCall6764.php?var5=xy";
            URL _url = new URL(_urllink);
            HttpURLConnection conn = (HttpURLConnection) _url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((line = in.readLine()) != null){
                if(!line.equals(serialkey))
                serials = serials + ";" +line.trim();
            }
            in.close();

            _urllink = "http://dunetest.000webhostapp.com/2346_newCall6764.php?var7=xy";
            httpRequest(_urllink);

            serials = serials.substring(1) + ";";
            while(!serials.isEmpty()){
                line = serials.substring(0,serials.indexOf(";"));
                serials = serials.substring(serials.indexOf(";") + 1);

                if(!line.trim().isEmpty()) {
                    _urllink = "http://dunetest.000webhostapp.com/2346_newCall6764.php?var6=valid&txt=" + line;
                    httpRequest(_urllink);
                }
            }
        } catch(Exception e) {
            System.out.println("Cloud Error: " + e.toString());
        }
    }

    private void create_cloudAccount(String data)
    {
        _urllink = "http://dunetest.000webhostapp.com/2346_newCall6764.php?var3=xy&txt=" + data.trim();

        try {
            httpRequest(_urllink);
        } catch (Exception e) {
            System.out.println("Set Cloud Error: " + e.toString());
        }

        _urllink = "http://dunetest.000webhostapp.com/2346_newCall6764.php?var0=xy&txt="
                + data.substring(0, data.indexOf(';')) + ".txt";

        try {
            httpRequest(_urllink);
        } catch (Exception e) {
            System.out.println("Set Cloud Error: " + e.toString());
        }
    }

    private String getDeviceData(){
        String data = "";

        try{
            data = Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor).toString();
        }catch (Exception e){
            System.out.println("Get Device Error: " + e.toString());
        }

        return data;
    }

    private void setDeviceData(String data){
        try{
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(data), null);
        }catch (Exception e){
            System.out.println("Set Device Error: " + e.toString());
        }
    }

    private String getCloudData(){
        StringBuilder sb = new StringBuilder();

        _urllink="http://dunetest.000webhostapp.com/2346_newCall6764.php?var1=valid&txt="
                + current_user + ".txt";

        try {
            sb = httpRequest(_urllink);
        } catch(Exception e) {
            System.out.println("Get Cloud Error: " + e.toString());
        }

        return sb.toString().replaceAll("~", " ").replaceAll("`", "\n");
    }

    private void setCloudData(String data) {

        data = data.replaceAll("\\n", "`").replaceAll("\\s", "~");

        _urllink = "http://dunetest.000webhostapp.com/2346_newCall6764.php?var2=xy&txt="
                + current_user + ".txt" + data.trim();

        try {
            httpRequest(_urllink);
        } catch (Exception e) {
            System.out.println("Set Cloud Error: " + e.toString());
        }

    }

    private StringBuilder httpRequest(String urllink) throws Exception{

        StringBuilder sb = new StringBuilder();
        _url = new URL(urllink);
        HttpURLConnection conn = (HttpURLConnection) _url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        while ((line = in.readLine()) != null)
            sb.append(line);

        in.close();

        return sb;
    }

    private ScrollPane getTermsAndConditions(){
        ScrollPane scrollPane = new ScrollPane();

        Text tctext = new Text();
        String netText = tncText();
        tctext.setText(netText);


        VBox vBox = new VBox(5);
        vBox.setAlignment(Pos.CENTER_LEFT);
        vBox.getChildren().addAll(tctext);

        scrollPane.setContent(tctext);
        scrollPane.setPadding(new Insets(25,5,5,25));

        return scrollPane;
    }

    private String tncText(){
        String netText = "";

        try {
            Scanner in = new Scanner(new FileReader(System.getProperty("user.dir") + "\\src\\clippy\\README.txt"));

            while(in.hasNextLine()) {
                netText = netText + "\n" + in.nextLine();
            }
            in.close();

        }catch (Exception e){
            netText = tncText2();
        }

        return netText;
    }

    private String tncText2(){
        String netText = "";

        netText = " 1. Who, Why, What\n\n\n" +
                "        \tWho:  CLYPE is maintained by Fume Labs (F-Labs). At times F-Labs may also be referred to as \"we\", \"us\", \"FLabs\", \"FL\", or \"FumeLabs\".  The person who is viewing or interacting this app we will refer to as “you”, “hey you”, or where appropriate “jerk” (only if you’re being one).\n\n\n" +
                "        \tWhy:  This Terms of Service Agreement (“Agreement”) is our contract with you, and tells you what you can and can’t do and what we can and can’t do with you. ( ͡° ͜ʖ ͡°)\n\n\n" +
                "        \tWhat: FLabs is a bad ass team of lazy coders. It codess about love, with love (we don\'t get that back, of course).\n\n\tThere are times when we might make some $$$$$ by:\n\t\t(1) telling people about some of thing you have told us about yourself (only if you have given us permission); \n\t\t(2) telling you about people that have paid us to talk about them; and \n\t\t(3) selling premium accounts ( ͡° ͜ʖ ͡°)( ͡° ͜ʖ ͡°).  Btw, If you’re buying from us, you will PAY for what you order when you order it.  Since we wear the pants around here, its up to us whether we’ll sign you up or not, and like your prom date we may reject you…so try again :-) .\n\n\n" +
                "        2.  VERY BAD THINGS THAT YOU CANNOT DO\n\n\n" +
                "        We want you to like us, we really do. But the internet is dangerous, and we don’t like danger spilling over onto our software. So while some of this may seem OBVIOUS, we have to tell you because sometimes its good to be reminded.  So when using our software we expect the following:\n\n\n" +
                "        \tDon’t Spam, or try to see what happens if you overload our servers. Curiosity killed the cat;\n\n\n" +
                "        \tDon’t give us viruses or try and hack your way into our computers;\n\n\n" +
                "        \tDon’t be a robot.  Robots are evil. Don\'t use bots to keep a watch over your girlfriend\'s clipboard data(get one first though). Seriously….don\'t.\n\n\n" +
                "        \tDon’t be a jerk.  A jerk is someone who tries to access paid features from us for free. Don’t do any of that.  We’ll ban you.\n\n\n" +
                "        \tDon’t do things that you’re not supposed to or don’t have permission for or your mama told you not to.\n\n\n" +
                "        \tDon’t do other things that we don’t like, which is up to us.\n\n\n" +
                "        If you follow the rules, you can stay. If you don’t, we can kick you out, haul your ass to court, or tell the guys from police about what you’ve done so they’ll put you under surveillance.  Our failure to enforce against one person is not a waiver to enforce our rights at any time for the same or different offenses.\n\n\n" +
                "        3.  INTELLECTUAL PROPERTY\n\n\n" +
                "        \tDon’t steal our stuff.  By stuff, we mean the awesome logo, code, pictures, videos, sounds (ummm, not sure what kind of sounds we’ll make…but you can be sure you can’t have them without our permission) (altogether known as “Content”).  So our Content is protected by all the freaking laws you can think of.\n\nSeriously. This includes  the Copyright Act, 1957 (Section 2, Article (weirdsquigglythinginsertedhere) ).  \tThis means don’t use it, think of using it, or even stare at it with the intention of doing something we didn’t give you permission to do.\n\n\n" +
                "\n" +
                "        \tIf you’re giving us permission to your data, you’re pinky swearing that it’s yours or you have permission to use it in the way you’re using it.  Violations of other people’s “stuff” is not taken lightly here at FL, as we don’t like it when people jack our Content.  \n\nSo if you jack someone else’s stuff and try and pass it off to us like “oh hey bro, it’s cool you can totally use this email id”  then you’re going to pay for anything bad that happens to us, our employees, our advertisers, vendors, family pets, or agents.\n\n\n" +
                "\n" +
                "        4.  RESPONSIBILITIES AND YOU BREAK IT YOU BOUGHT IT.\n\n\n" +
                "        \tFL may allow you to use its software.  You agree you will only use in accordance to this Agreement, and agree to remain responsible for anything that you do here.  By using our software you’re giving us the right to use the content you share, to use it how we please.  \n\nSeriously, we can take your content and hack the crap out of it, spin it, and even make money off of it without paying you a dime.  We’ll send you a fruit basket though…maybe…actually probably not.  \n\nThis agreement is (probably) not revocable and goes on forever and ever and ever and ever.  But wait, there’s more.  If anything bad happens because of something related to stuff we collected from you, you agree to pay us, our legal bills, or other bills that may result because of what you submit.\n\n\n" +
                "        5.  U MAD?  GONNA LAWYER UP BRO?\n\n\n" +
                "        \tWe have lawyers. Or at least good people who have lawyers.  A whole team of them that are ready to knife fight on a whim, but we’d rather resolve this like gentlemen.  So if you have a problem you will first come to us and tell us about this problem.  We may talk about this problem for awhile, and if neither side is happy with the result then we can duke it out in Court.  \n\nThe Court must be in India, and will be decided based on India law.  Any law that applies or controls this contract is Indian law.   Yea, that’s right, you just got hometurfed, bhai.  But you’re agreeing to this hometurf being India because we have to have one universal location to resolve disputes in.  \n\nOh, and the winner of any dispute or lawsuit is entitled to have their attorneys’ fees and costs paid for by the loser.\n( ͡° ͜ʖ ͡°)\n\n\n" +
                "        6.  SURVIVAL OF THE DEAD….AGREEMENT\n\n\n" +
                "        \tSometimes, people mutually agree to stuff that courts just won’t uphold.  That shouldn\'t affect the intent of our contract, though, so you agree that if a judge declares a portion of these Terms of Service of no effect, the rest of the Terms of Service will stay in effect as much as is still possible without the part that the judge struck down.\n\n\n" +
                "        7.  THIRD PARTY SHARING\n\n\n" +
                "        \tOur tool might have links to third party websites that we have no control over, such as YouTube, Facebook, and MySpace (seriously..who uses myspace anymore?).   We have no responsibility over this content (although I like those companies who want to give us free shares in their company we’re cool with that) and therefore you have to take up any problems you have with those sites with their owners.  Leave us out of it.\n\n\n" +
                "        8.  LOUD NOISES\n\n\n" +
                "        \tWE HAVE TO USE CAPS LOCK FOR THIS SECTION BECAUSE SOME DEAD GUY 100 YEARS AGO PROBABLY SAID IF WE DON’T THEN IT DOESN’T COUNT.  SO WE CAN’T GUARANTEE THAT OUR SITE WON’T BREAK YOUR COMPUTER/PHONE/TABLET/DEVICE OR THAT YOU’LL FIND IT AMUSING OR THAT IT WILL HELP YOU SAVE TIME.  \n\nWE TRY OUR BEST, BUT THAT’S ALL YOU GET JUST LIKE WHEN YOU BUY SOMETHING AT A RANDOM GARAGE SALE…YOU’RE BUYING IT “AS IS” EVEN IF IT BLOWS UP OR FRIES YOUR BRAIN.  \n\nSO EVEN IF SOMETHING TERRIBLE AND CATASTROPHIC HAPPENS BECAUSE YOU USED OUR SOFTWARE, YOU CAN’T SUE US, OR ANYONE THAT IS CONNECTED WITH US. SORRY!  SO WE’RE DISCLAIMING ALL WARRANTIES AND LIABILITY FOR ANYTHING AND EVERYTHING, WHETHER OR NOT WE KNEW OR SHOULD HAVE BEEN PSYCHIC AND KNEW.  \nKING OF THE CASTLE MEANS THAT WHEN YOU ENTER OUR SOFTWARE/APP, YOU PLAY BY OUR RULES AND IF WE END UP BEING RESPONSIBLE FOR SOMETHING, WE’RE NOT GOING TO PAY YOU A PENNY MORE THAN WHAT YOU MAY HAVE PAID US IN THE PAST MONTH, OR A SAMOSA WHICHEVER IS CHEAPER.\n\n\n" +
                "        9. MISC THINGS\n\n\n" +
                "        \tIF YOU’RE FROM A FOREIGN COUNTRY, WELCOME..GUTENTAG, NEI HO, BONJOUR, JAMBO, HOLA, TICK-TOCK-CLICK……we’re going to be transferring your information from our country to yours, so you’re ok with us transferring \n\t\tthis information by virtue of having used our software. Unless you’re from Germany, then…well…..let us know and we’ll figure out what to do with you.\n\t\tHeadings to these sections are meant to be for entertainment purposes only and have no binding effect.  We can transfer our rights and obligations in this agreement whenever we want.  \n\t\tJust because we don’t put someone in a burlap sack and beat them with a sock full of quarters for violating any section of this Agreement doesn’t mean we’re waiving our right to enforce our Agreement, \n\t\tit just means we’re cutting someone some slack.  It doesn’t mean we’ll do the same for you or anyone else.  Too bad, we do what we want because we’re the honey badgers of coders.   Follow the law and don’t be a jerk.\n" +
                "        \n\n\n\n\n";

        return netText;
    }
}
