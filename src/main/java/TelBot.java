
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.FileWriter;
import java.time.Duration;
import java.util.List;


public class TelBot extends TelegramLongPollingBot {

    private static ListStorage listStorage = new ListStorage();

    @Override
    public void onUpdateReceived(Update update) {

        SendMessage sndMessage = new SendMessage();

        if (update.hasMessage()) {

            String command = update.getMessage().getText();
            Message rcvMessage = update.getMessage();

            if (rcvMessage.isCommand()) {

                if (command.startsWith(Command.EPIC)) {

                    listStorage.addUrl("https://novi.kupujemprodajem.com/knjige/epska-fantastika/grupa/8/1095/");

                    sndMessage.setText("Tražim u Epskoj Fantastici...\n Unesi naziv knjige: ");

                    sndMessage.setChatId(update.getMessage().getChatId());

                    try {
                        execute(sndMessage);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }

                } else if (command.startsWith(Command.SCIENCE)) {

                    listStorage.addUrl("https://novi.kupujemprodajem.com/knjige/naucna-fantastika/grupa/8/355/");

                    sndMessage.setText("Tražim u Naučnoj Fantastici...\n Unesi naziv knjige: ");

                    sndMessage.setChatId(update.getMessage().getChatId());

                    try {
                        execute(sndMessage);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                sndMessage.setText("Tražim Knjigu: " + command);

                sndMessage.setChatId(update.getMessage().getChatId());

                try {
                    execute(sndMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

                String url = listStorage.getUrlList().get(0);
                //System.out.println(url);
                //System.out.println(command);
                listStorage.removeUrl();

                scrape(url, command);

                if (listStorage.bookListSize()==0) {

                    sndMessage.setText("Nema tražene knjige.");

                    sndMessage.setChatId(update.getMessage().getChatId());

                    try {
                        execute(sndMessage);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }

                } else {

                    for (int i = 0; i < listStorage.bookListSize(); i++) {
                        String bookNamePricePage = listStorage.printBookList(i);

                        sndMessage.setText(bookNamePricePage);

                        sndMessage.setChatId(update.getMessage().getChatId());

                        try {
                            execute(sndMessage);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    }
                }

                listStorage.removeBook();

            }

        }

    }

    @Override
    public String getBotUsername() {
        return "skrejp_bot";
    }

    @Override
    public String getBotToken() {
        return "5914952957:AAHMHDBQCReDVrxHLPrrcMNXrZoUknpKlOg";
    }

    static void scrape(String url, String book) {

        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions options = new FirefoxOptions(); // PRAVIM OPTIONS ZA HEADLESS MODE
        options.setHeadless(true); // SET HEADLESS - TRUE
        WebDriver driver = new FirefoxDriver(options); // POZIVAM OPTIONS
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(120));
        //writer = new FileWriter("C:\\Users\\EliteBook\\Desktop\\" + book + ".txt", true); //SETTING TXT FILE

        int pageNum = 25;

        while (true) {

            driver.get(url + pageNum + "?page=" + pageNum);

            List<WebElement> bookListing = driver.findElements(By.className("AdItem_adHolder__NoNLJ"));

            for (WebElement listing : bookListing) {

                String title = listing.findElement(By.className("AdItem_name__80tI5")).getText();
                String price = listing.findElement(By.className("AdItem_price__jUgxi")).getText();

                if (title.toLowerCase().contains(book.toLowerCase())) {
                    //System.out.println("Knjiga --> " + title + " --> " + price + " --> str " + pageNum);
                    //writer.write("Knjiga --> " + title + " --> " + price + " --> str " + pageNum + "\n"); //PISE TXT FAJL

                    listStorage.addBook(title, price, pageNum);
                }
            }

            List<WebElement> arrows = driver.findElements(By.className("Button_trailing__CU1T2"));
            if (arrows.size() == 2) {
                pageNum++;
            } else {
                //int bookCount = listStorage.bookListSize(); // PROVERAVAM KOLIKO IMA KNJIGA U LISTI
                //System.out.println(listStorage.getBookList()); //ISPISUJE LISTU KNJIGA
                driver.quit();
                break;
            }
        }

    }
}
