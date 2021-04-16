module.exports = [
  {
    type: 'list',
    name: 'preProcessor',
    message: 'CSS Pre-processor?',
    choices: [
      {
        name: 'SCSS',
        value: 'scss'
      },
      {
        name: 'SASS',
        value: 'sass'
      },
      {
        name: 'Stylus',
        value: 'stylus'
      },
      {
        name: 'Less',
        value: 'less'
      }
    ]
  }
]
