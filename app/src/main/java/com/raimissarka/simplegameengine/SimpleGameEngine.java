package com.raimissarka.simplegameengine;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class SimpleGameEngine extends Activity {

    // gameView will be the view of the game
    // It will also hold the logic of the game
    // and respond to screen touches as well
    GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize gameView and set it as the view
        gameView = new GameView(this);
        setContentView(gameView);

    }

    // GameView class will go here

    // Here is our implementation of GameView
    // It is an inner class.
    // Note how the final closing curly brace }
    // is inside SimpleGameEngine

    // Notice we implement runnable so we have
    // A thread and can override the run method.
    class GameView extends SurfaceView implements Runnable, SurfaceHolder.Callback{

        // This is our thread
        Thread gameThread = null;

        // This is new. We need a SurfaceHolder
        // When we use Paint and Canvas in a thread
        // We will see it in action in the draw method soon.
        SurfaceHolder ourHolder;

        // A boolean which we will set and unset
        // when the game is running- or not.
        volatile boolean playing;

        // A Canvas and a Paint object
        Canvas canvas;
        Paint paint;

        // This variable tracks the game frame rate
        long fps;

        // This is used to help calculate the fps
        private long timeThisFrame;

        // Declare an object of type Bitmap
        Bitmap bitmapBob;
        Bitmap cropedImage;
        Bitmap buttonToRight;
        Bitmap buttonToLeft;

        // Bob starts off not moving
        boolean isMoving = false;

        //Set moving direction
        boolean movingToRight = true;
        boolean movingToLeft = false;

        //if false - image collumns is counting from left to right, else form right to left
        boolean stepImageDirection = false;

        // He can walk at 150 pixels per second
        float walkSpeedPerSecond = 150;

        public int wholeImageWidth;
        public int wholeImageHeight;

        public int imageCropWidth;
        public int imageCropHeight;

        public int col;
        public int row;

        public int[] buttonToLeftArea = {0, 0, 0, 0};
        public int[] buttonToRightArea = {0, 0, 0, 0};

        // He starts 10 pixels from the left
        public final static int startPositionX  = 10;

        float bobXPosition = startPositionX;
        float nextImagePosition;
        float nextCol;

        // When the we initialize (call new()) on gameView
        // This special constructor method runs
        public GameView(Context context) {
            // The next line of code asks the
            // SurfaceView class to set up our object.
            // How kind.
            super(context);

            // Initialize ourHolder and paint objects
            ourHolder = getHolder();
            paint = new Paint();

            // Load Bob from his .png file
            bitmapBob = BitmapFactory.decodeResource(this.getResources(), R.drawable.chibi1);
            //Load control buttons
            buttonToLeft = BitmapFactory.decodeResource(this.getResources(), R.drawable.button_go_to_the_left);
            buttonToRight = BitmapFactory.decodeResource(this.getResources(), R.drawable.button_go_to_right);

            wholeImageWidth = bitmapBob.getWidth();
            wholeImageHeight = bitmapBob.getHeight();

            buttonToLeftArea [0] = 10;
            buttonToLeftArea [1] = wholeImageHeight + 40;
            buttonToLeftArea [2] = buttonToLeft.getWidth() + buttonToLeftArea[0];
            buttonToLeftArea [3] = buttonToLeft.getHeight() + buttonToLeftArea[2];

            buttonToRightArea [0] = 310;
            buttonToRightArea [1] = wholeImageHeight + 40;
            buttonToRightArea [2] = buttonToRight.getWidth() + buttonToRightArea[0];
            buttonToRightArea [3] = buttonToRight.getHeight() + buttonToRightArea[2];

            imageCropWidth = wholeImageWidth / 3;
            imageCropHeight = wholeImageHeight / 4;
            nextCol = (imageCropWidth + startPositionX) / 3;
            nextImagePosition = nextCol;

            row = 2;
            col = 0;

            cropedImage = Bitmap.createBitmap(bitmapBob, imageCropWidth * col,
                    imageCropHeight * row, imageCropWidth, imageCropHeight);
            // Set our boolean to true - game on!
            playing = true;

        }

        public Bitmap GameViewUpdate (int c, int r){
            bitmapBob = BitmapFactory.decodeResource(this.getResources(), R.drawable.chibi1);

            wholeImageWidth = bitmapBob.getWidth();
            wholeImageHeight = bitmapBob.getHeight();

            imageCropWidth = wholeImageWidth / 3;
            imageCropHeight = wholeImageHeight / 4;

            cropedImage = Bitmap.createBitmap(bitmapBob, imageCropWidth * col,
                    imageCropHeight * row, imageCropWidth, imageCropHeight);

            return cropedImage;
        }


        @Override
        public void run() {
            while (playing) {

                // Capture the current time in milliseconds in startFrameTime
                long startFrameTime = System.currentTimeMillis();

                // Update the frame
                update();

                // Draw the frame
                draw();

                // Calculate the fps this frame
                // We can then use the result to
                // time animations and more.
                timeThisFrame = System.currentTimeMillis() - startFrameTime;
                if (timeThisFrame > 0) {
                    fps = 1000 / timeThisFrame;
                }

            }

        }

        // Everything that needs to be updated goes in here
        // In later projects we will have dozens (arrays) of objects.
        // We will also do other things like collision detection.
        public void update() {

            // If bob is moving (the player is touching the screen)
            // then move him to the right based on his target speed and the current fps.
            if(isMoving){
                bobXPosition = bobXPosition + (walkSpeedPerSecond / fps);
                if(bobXPosition > nextImagePosition ) {
                    if (!stepImageDirection) {
                        gameView.col++;
                    } else {
                        gameView.col--;
                    }
                    nextImagePosition = nextImagePosition + nextCol;
                }
                if (gameView.col>=2){
                    //revert direction
                    stepImageDirection = true;
                } else if (gameView.col <=0) {
                    stepImageDirection = false;
                }

                cropedImage = GameViewUpdate(col, row);
            }

        }

        // Draw the newly updated scene
        public void draw() {

            // Make sure our drawing surface is valid or we crash
            if (ourHolder.getSurface().isValid()) {
                // Lock the canvas ready to draw
                canvas = ourHolder.lockCanvas();

                // Draw the background color
                canvas.drawColor(Color.argb(255,  26, 128, 182));

                // Choose the brush color for drawing
                paint.setColor(Color.argb(255,  249, 129, 0));

                // Make the text a bit bigger
                paint.setTextSize(45);

                // Display the current fps on the screen
                canvas.drawText("FPS:" + fps, 20, 40, paint);
                canvas.drawText("row" + row, 20, 80, paint);
                canvas.drawText("col" + col, 20, 120, paint);
                canvas.drawText("imageCropWidth" + imageCropWidth, 120, 40, paint);
                canvas.drawText("imageCropHeight" + imageCropHeight, 120, 80, paint);
                canvas.drawText("imageWidth" + bitmapBob.getWidth(), 120, 120, paint);
                canvas.drawText("bobXposition" + bobXPosition, 20, 160, paint);

                // Draw bob at bobXPosition, 200 pixels
                canvas.drawBitmap(gameView.cropedImage, bobXPosition, 200, paint);
                canvas.drawBitmap(buttonToLeft, buttonToLeftArea[0], buttonToLeftArea[1], paint);
                canvas.drawBitmap(buttonToRight, buttonToRightArea[0], buttonToRightArea[1], paint);

                // Draw everything to the screen
                ourHolder.unlockCanvasAndPost(canvas);
            }

        }

        // If SimpleGameEngine Activity is paused/stopped
        // shutdown our thread.
        public void pause() {
            playing = false;
            try {
                gameThread.join();
            } catch (InterruptedException e) {
                Log.e("Error:", "joining thread");
            }

        }

        // If SimpleGameEngine Activity is started then
        // start our thread.
        public void resume() {
            playing = true;
            gameThread = new Thread(this);
            gameThread.start();
        }

        // The SurfaceView class implements onTouchListener
        // So we can override this method and detect screen touches.
        @Override
        public boolean onTouchEvent(MotionEvent motionEvent) {

            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {

                // Player has touched the screen
                case MotionEvent.ACTION_DOWN:

                    // Set isMoving so Bob is moved in the update method
                    int x=  (int)motionEvent.getX();
                    int y = (int)motionEvent.getY();
                    if ( x > buttonToRightArea[0] && x < buttonToRightArea [2]
                            && y > buttonToRightArea[1] && y < buttonToRightArea[3]) {
                        isMoving = true;
                    }
                    break;

                // Player has removed finger from screen
                case MotionEvent.ACTION_UP:

                    // Set isMoving so Bob does not move
                    isMoving = false;

                    break;
            }
            return true;
        }

        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {

        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

        }
    }
    // This is the end of our GameView inner class

    // More SimpleGameEngine methods will go here

    // This method executes when the player starts the game
    @Override
    protected void onResume() {
        super.onResume();

        // Tell the gameView resume method to execute
        gameView.resume();
    }

    // This method executes when the player quits the game
    @Override
    protected void onPause() {
        super.onPause();

        // Tell the gameView pause method to execute
        gameView.pause();
    }

}