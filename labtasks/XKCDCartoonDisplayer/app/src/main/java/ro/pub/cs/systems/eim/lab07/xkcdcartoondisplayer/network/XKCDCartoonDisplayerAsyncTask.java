package ro.pub.cs.systems.eim.lab07.xkcdcartoondisplayer.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.ResponseHandler;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.BasicResponseHandler;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import ro.pub.cs.systems.eim.lab07.xkcdcartoondisplayer.entities.XKCDCartoonInformation;
import ro.pub.cs.systems.eim.lab07.xkcdcartoondisplayer.general.Constants;

public class XKCDCartoonDisplayerAsyncTask extends AsyncTask<String, Void, XKCDCartoonInformation> {

    private TextView xkcdCartoonTitleTextView;
    private ImageView xkcdCartoonImageView;
    private TextView xkcdCartoonUrlTextView;
    private Button previousButton, nextButton;

    private class XKCDCartoonButtonClickListener implements Button.OnClickListener {

        private String xkcdComicUrl;

        public XKCDCartoonButtonClickListener(String xkcdComicUrl) {
            this.xkcdComicUrl = xkcdComicUrl;
        }

        @Override
        public void onClick(View view) {
            new XKCDCartoonDisplayerAsyncTask(xkcdCartoonTitleTextView, xkcdCartoonImageView, xkcdCartoonUrlTextView, previousButton, nextButton).execute(xkcdComicUrl);
        }

    }

    public XKCDCartoonDisplayerAsyncTask(TextView xkcdCartoonTitleTextView, ImageView xkcdCartoonImageView, TextView xkcdCartoonUrlTextView, Button previousButton, Button nextButton) {
        this.xkcdCartoonTitleTextView = xkcdCartoonTitleTextView;
        this.xkcdCartoonImageView = xkcdCartoonImageView;
        this.xkcdCartoonUrlTextView = xkcdCartoonUrlTextView;
        this.previousButton = previousButton;
        this.nextButton = nextButton;
    }

    @Override
    public XKCDCartoonInformation doInBackground(String... urls) {
        XKCDCartoonInformation xkcdCartoonInformation = new XKCDCartoonInformation();

        // TODO exercise 5a)
        // 1. obtain the content of the web page (whose Internet address is stored in urls[0])
        // - create an instance of a HttpClient object
        // - create an instance of a HttpGet object
        // - create an instance of a ResponseHandler object
        // - execute the request, thus obtaining the web page source code

        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGetXKCD = new HttpGet(urls[0]);
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        String pageSourceCode = null;

        try {
            pageSourceCode = httpClient.execute(httpGetXKCD, responseHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 2. parse the web page source code
        // - cartoon title: get the tag whose id equals "ctitle"
        // - cartoon url
        //   * get the first tag whose id equals "comic"
        //   * get the embedded <img> tag
        //   * get the value of the attribute "src"
        //   * prepend the protocol: "http:"
        // - cartoon bitmap (only if using Apache HTTP Components)
        //   * create the HttpGet object
        //   * execute the request and obtain the HttpResponse object
        //   * get the HttpEntity object from the response
        //   * get the bitmap from the HttpEntity stream (obtained by getContent()) using Bitmap.decodeStream() method
        // - previous cartoon address
        //   * get the first tag whole rel attribute equals "prev"
        //   * get the href attribute of the tag
        //   * prepend the value with the base url: http://www.xkcd.com
        //   * attach the previous button a click listener with the address attached
        // - next cartoon address
        //   * get the first tag whole rel attribute equals "next"
        //   * get the href attribute of the tag
        //   * prepend the value with the base url: http://www.xkcd.com
        //   * attach the next button a click listener with the address attached

        if(pageSourceCode != null) {
            Document document = Jsoup.parse(pageSourceCode);
            Element htmlTag = document.child(0);

            // extract title
            Element divTagIdCtitle = htmlTag.getElementsByAttributeValue(Constants.ID_ATTRIBUTE, Constants.CTITLE_VALUE).first();
            xkcdCartoonInformation.setCartoonTitle(divTagIdCtitle.ownText());

            // extract image url
            Element divTagIdComic = htmlTag.getElementsByAttributeValue(Constants.ID_ATTRIBUTE, Constants.COMIC_VALUE).first();
            String cartoonInternetAddress = divTagIdComic.getElementsByTag(Constants.IMG_TAG).attr(Constants.SRC_ATTRIBUTE);
            String cartoonUrl = Constants.HTTP_PROTOCOL + cartoonInternetAddress;
            xkcdCartoonInformation.setCartoonUrl(cartoonUrl);

            // extract bitmap from obtained image url
            HttpGet httpGet = new HttpGet(cartoonUrl);
            try {
                HttpResponse httpResponse = httpClient.execute(httpGet);
                HttpEntity httpEntity = httpResponse.getEntity();
                if(httpEntity != null) {
                    // get the bitmap from the HttpEntity stream (obtained by getContent()) using Bitmap.decodeStream() method
                    xkcdCartoonInformation.setCartoonBitmap(BitmapFactory.decodeStream(httpEntity.getContent()));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            Element prevRel = htmlTag.getElementsByAttributeValue(Constants.REL_ATTRIBUTE, Constants.PREVIOUS_VALUE).first();
            String prevUrl = Constants.XKCD_INTERNET_ADDRESS + prevRel.attr(Constants.HREF_ATTRIBUTE);
            xkcdCartoonInformation.setPreviousCartoonUrl(prevUrl);

            Element nextRel = htmlTag.getElementsByAttributeValue(Constants.REL_ATTRIBUTE, Constants.NEXT_VALUE).first();
            String nextUrl = Constants.XKCD_INTERNET_ADDRESS + nextRel.attr(Constants.HREF_ATTRIBUTE);
            xkcdCartoonInformation.setNextCartoonUrl(nextUrl);
        }

        return  xkcdCartoonInformation;
    }

    @Override
    protected void onPostExecute(final XKCDCartoonInformation xkcdCartoonInformation) {

        // TODO exercise 5b)

        if(xkcdCartoonInformation == null) {
            return;
        }

        String prevUrl = xkcdCartoonInformation.getPreviousCartoonUrl();
        String nextUrl = xkcdCartoonInformation.getNextCartoonUrl();

        String cartoonTitle = xkcdCartoonInformation.getCartoonTitle();
        String cartoonUrl = xkcdCartoonInformation.getCartoonUrl();
        Bitmap cartoonBitmap = xkcdCartoonInformation.getCartoonBitmap();

        if(cartoonTitle != null) {
            xkcdCartoonTitleTextView.setText(xkcdCartoonInformation.getCartoonTitle());
        }

        if(cartoonBitmap != null) {
            xkcdCartoonImageView.setImageBitmap(xkcdCartoonInformation.getCartoonBitmap());
        }

        if(cartoonUrl != null) {
            xkcdCartoonUrlTextView.setText(cartoonUrl);
        }

        if(prevUrl != null) {
            previousButton.setOnClickListener(new XKCDCartoonButtonClickListener(prevUrl));
        }

        if(nextUrl != null) {
            nextButton.setOnClickListener(new XKCDCartoonButtonClickListener(nextUrl));
        }

    }

}
