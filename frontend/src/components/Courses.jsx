export default function Courses({ name, teacher, attendance, setModal }) {
    return (
      <div className="course-info">
        <div className="course-name">
            <h1>{name}</h1>
        </div>
        <div className="course-teacher-att">
            <h2>{teacher}</h2>
            <p>Attendance: {attendance}</p>
            <input className="course-show-more-btn" type="button" onClick={() => setModal(true)} value='Show more'/>
        </div>
      </div>
    );
  }
  