# slr
Sign Language Recognition app

TODO:

- improve training examples collection feature.
- create separate tool to generate training data file from raw images.
- implement "train model" workflow
    - separate application to train and run experiments with various configurations.
    - Maybe extract a distinct "prediction" maven module, and make the main app, and the training app depend on it;

- use external java library for ANN.

- generate more training examples and build a larger training set
- commit training set

- review image processing algorithms
- make a better analysis of the learning performance

- logging
- unit tests
- improve exception handling

- reimplement pause/resume feature? or just drop the feature?
- reimplement on click on small image -> enlarge it  (on click, change the ImageTransformer associated to the web cam
- move neural network package location?

- investigate why on stop camera, sometimes we get a NPE. (most likely a thread synchronization issue)